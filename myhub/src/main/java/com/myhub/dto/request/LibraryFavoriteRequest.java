package com.myhub.dto.request;

import jakarta.validation.constraints.NotNull;

public class LibraryFavoriteRequest {
    @NotNull(message = "favorite 不能为空")
    private Boolean favorite;

    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }
}
