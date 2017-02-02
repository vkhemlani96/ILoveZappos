package com.vinaykhemlani.ilovezappos.API;

/**
 * Created by Vinay on 2/1/17.
 *  This is used to map the JSON keys from the individual search results
 *  to a Java class.
 *
 *  All functions are getters for class variables.
 *  toString function prints class variables
 */

public class SearchResult {

    String brandName;
    String thumbnailImageUrl;
    String productId;
    String originalPrice;
    String styleId;
    String colorId;
    String price;
    String percentOff;
    String productUrl;
    String productName;

    public String getBrandName() {
        return brandName;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getStyleId() {
        return styleId;
    }

    public String getColorId() {
        return colorId;
    }

    public String getPrice() {
        return price;
    }

    public String getPercentOff() {
        return percentOff;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getProductName() {
        return productName;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "brandName='" + brandName + '\'' +
                ", thumbnailImageUrl='" + thumbnailImageUrl + '\'' +
                ", productId='" + productId + '\'' +
                ", originalPrice='" + originalPrice + '\'' +
                ", styleId='" + styleId + '\'' +
                ", colorId='" + colorId + '\'' +
                ", price='" + price + '\'' +
                ", percentOff='" + percentOff + '\'' +
                ", productUrl='" + productUrl + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}
