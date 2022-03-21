package com.star.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Author: zzStar
 * @Date: 03-20-2022 22:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkResult {

    private Boolean isExited;

    private Set<Integer> uploadedList;

    public ChunkResult(boolean isExited) {
        this.isExited = isExited;
    }

}
