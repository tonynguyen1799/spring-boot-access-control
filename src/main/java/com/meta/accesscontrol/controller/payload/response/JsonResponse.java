package com.meta.accesscontrol.controller.payload.response;

public record JsonResponse<T>(
    int status,
    String message,
    T data
) {}