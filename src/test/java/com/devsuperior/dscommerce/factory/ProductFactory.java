package com.devsuperior.dscommerce.factory;

import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;

public class ProductFactory {
    public static Product createProduct() {
        Category category = CategoryFactory.createCategory();
        Product product = new Product(
                1L,
                "Console PlayStation 5",
                "Lorem ipsum",
                3000.0,
                "https://example.com.br"
                );
        product.getCategories().add(category);
        return product;
    }

    public static Product createProduct(String name) {
        Product product = createProduct();
        product.setName(name);
        return product;
    }
}
