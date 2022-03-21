<script>
import axios from 'axios'
import md5 from 'spark-md5'

export default {
  data() {
    return {
      isExited: false,
      options: {
        target: '//localhost:8989/upload/chunk', // 目标上传 URL
        parseTimeRemaining: function (timeRemaining, parsedTimeRemaining) {
          return parsedTimeRemaining.replace(/\syears?/, '年').
            replace(/\days?/, '天').
            replace(/\shours?/, '小时').
            replace(/\sminutes?/, '分钟').
            replace(/\sseconds?/, '秒')
        },
        // 根据 XHR 响应内容检测每个块是否上传成功了
        // Chunk 实例以及请求响应信息
        checkChunkUploadedByResponse: (chunk, message) => {
          const ret = JSON.parse(message)
          console.log('ret: ', ret)
          if (ret.data.isExited) {
            this.isExited = true
            return true
          }
          return (ret.data.uploadedList || []).indexOf(chunk.offset + 1) >= 0
        },
      },
      attrs: {
        accept: 'image/*',
      },
      statusText: {
        success: '上传成功',
        error: '上传出错了',
        uploading: '上传中...',
        paused: '暂停中...',
        waiting: '等待中...',
        cmd5: '计算文件MD5中...',
      },
      fileList: [], // 对待文件、文件夹列表
      disabled: true,
      fileTotal: 0, // 本次文件上传的总数
      errorFileList: [], // 上传失败信息列表
      errorFileDialog: false,
      uploadType: ['pdf', 'zip'],
    }
  },
  watch: {
    fileList(o, n) {
      this.disabled = false
    },
  },
  methods: {
    // 首先检验文件格式，添加到文件列表
    onFileAdded: function (file) {
      let type = file.name.substring(file.name.lastIndexOf('.') + 1)
      console.log('type: ', type)
      if (this.uploadType.indexOf(type) === -1) {
        this.$notify.error({
          title: '失败',
          message: '文件不是规定格式',
        })
        file.ignored = true
      } else {
        file.pause()
      }
      this.$nextTick(() => {
        this.fileTotal = this.$refs['uploader'].files.length
      })
      if (this.errorFileList.length !== 0) {
        this.errorFileDialog = true
      }
    },
    // 每个文件传输后，返回的信息
    onFileSuccess(rootFile, file, resp, chunk) {
      let res = JSON.parse(resp)
      if (res.flag && !this.isExited) {
        axios.post('/api/upload/merge', {
          identifier: file.uniqueIdentifier,
          filename: file.name,
          totalChunks: chunk.offset,
        }).then(({ data }) => {
          if (data.flag) {
            this.$notify.success({
              title: '成功',
              message: data.message,
            })
          } else {
            this.$notify.error({
              title: '失败',
              message: data.message,
            })
          }
        })
      } else {
        this.$notify.success({
          title: '上传成功，不需要合并',
        })
      }
      if (this.isExited) {
        this.isExited = false
      }
    },
    filesAdded(file, fileList) {
      file.forEach(e => {
        this.fileList.push(e)
        this.computeMd5(e)
      })
    },
    onFileProgress() {},
    onFileError() {},
    onFileRemove() {},
    computeMd5(file) {
      let fileReader = new FileReader()
      let time = new Date().getTime()
      let blob = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
      let currentChunk = 0
      const chunkSize = 1024 * 1024
      let chunks = Math.ceil(file.size / chunkSize)
      let spark = new md5.ArrayBuffer()
      file.cmd5 = true
      file.pause()
      loadNext()

      fileReader.onload = e => {
        spark.append(e.target.result)
        if (currentChunk < chunks) {
          currentChunk++
          loadNext()
          console.log(
            `第${ currentChunk }分片解析完成, 开始第${
              currentChunk + 1
            } / ${ chunks }分片解析`,
          )
        } else {
          let md5 = spark.end()
          console.log(
            `MD5计算完毕：${ file.name } \nMD5: ${ md5 } \n分片: ${ chunks } 大小: ${
              file.size
            } 用时：${ new Date().getTime() - time } ms`,
          )
          spark.destroy() // 释放缓存
          file.uniqueIdentifier = md5 // 将文件md5赋值给文件唯一标识
          file.cmd5 = false // 取消计算md5状态
          file.resume() // 开始上传
        }
      }
      fileReader.onerror = () => {
        this.$notify.error({
          title: `文件${ file.name }读取出错，请检查该文件`,
        })
        file.cancel()
      }

      function loadNext() {
        let start = currentChunk * chunkSize
        let end =
          start + chunkSize >= file.size ? file.size : start + chunkSize
        fileReader.readAsArrayBuffer(blob.call(file.file, start, end))
      }
    },
    allStart() {
      this.fileList.map((e) => {
        if (e.paused) {
          e.resume()
        }
      })
    },
    allStop() {
      this.fileList.map((e) => {
        if (!e.paused) {
          e.pause()
        }
      })
    },
    allRemove() {
      this.fileList.map((e) => {
        e.cancel()
      })
      this.fileList = []
    },
  },
}
</script>

<template>
  <div>
    <uploader
      ref="uploader"
      :options="options"
      :autoStart="false"
      :file-status-text="statusText"
      class="uploader"
      @file-added="onFileAdded"
      @files-added="filesAdded"
      @file-success="onFileSuccess"
      @file-progress="onFileProgress"
      @file-error="onFileError"
      @file-remove="onFileRemove"
    >
      <uploader-unsupport/>
      <uploader-drop>
        <p>Drop files here to upload or</p>
        <uploader-btn style="background-color: #eab4b6">select files</uploader-btn>
        <uploader-btn :attrs="attrs">select images</uploader-btn>
        <uploader-btn :directory="true" style="background-color: #0a95ec">select folder</uploader-btn>
      </uploader-drop>
      <uploader-files/>
    </uploader>
    <button @click="allStart()" :disabled="disabled">全部开始</button>
    <button @click="allStop()">全部暂停</button>
    <button @click="allRemove()">全部移除</button>
  </div>
</template>
