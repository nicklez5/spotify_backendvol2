package com.spotify11.demo.dtos;

public record PlaylistSummaryDto(
    Integer id,
    String  name,
    String  ownerName,
    Integer trackCount,
    String  visibility
) {}