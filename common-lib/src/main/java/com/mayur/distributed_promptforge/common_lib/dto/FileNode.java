package com.mayur.distributed_promptforge.common_lib.dto;


public record FileNode(
        String path
) {

    @Override
    public String toString() {
        return path;
    }
}