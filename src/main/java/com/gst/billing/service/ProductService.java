package com.gst.billing.service;

import com.gst.billing.dto.ProductDto;
import com.gst.billing.entity.Product;
import com.gst.billing.exception.ResourceNotFoundException;
import com.gst.billing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for product CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /** Return all products */
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    /** Find a single product by ID */
    public ProductDto getProductById(Long id) {
        return toDto(findOrThrow(id));
    }

    /** Create a new product */
    public ProductDto createProduct(ProductDto dto) {
        Product product = Product.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .gstPercentage(dto.getGstPercentage())
            .hsnCode(dto.getHsnCode())
            .build();
        return toDto(productRepository.save(product));
    }

    /** Update an existing product */
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = findOrThrow(id);
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setGstPercentage(dto.getGstPercentage());
        product.setHsnCode(dto.getHsnCode());
        return toDto(productRepository.save(product));
    }

    /** Delete a product */
    public void deleteProduct(Long id) {
        findOrThrow(id);
        productRepository.deleteById(id);
    }

    // ---- Internal helpers ----

    public Product findOrThrow(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setGstPercentage(p.getGstPercentage());
        dto.setHsnCode(p.getHsnCode());
        return dto;
    }
}
