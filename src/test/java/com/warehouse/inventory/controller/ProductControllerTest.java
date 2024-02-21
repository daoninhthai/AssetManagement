package com.warehouse.inventory.controller;

import com.warehouse.inventory.model.Category;
import com.warehouse.inventory.model.Product;
import com.warehouse.inventory.model.Supplier;
import com.warehouse.inventory.service.CategoryService;
import com.warehouse.inventory.service.ProductService;
import com.warehouse.inventory.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private SupplierService supplierService;

    private Product sampleProduct;
    private Category sampleCategory;
    private Supplier sampleSupplier;
    private List<Product> productList;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Electronics");

        sampleSupplier = new Supplier();
        sampleSupplier.setId(1L);
        sampleSupplier.setName("Acme Corp");

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setSku("ELEC-001");
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setDescription("Ergonomic wireless mouse");
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setMinStockLevel(10);
        sampleProduct.setCategory(sampleCategory);
        sampleProduct.setSupplier(sampleSupplier);

        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setSku("ELEC-002");
        secondProduct.setName("Mechanical Keyboard");
        secondProduct.setPrice(new BigDecimal("79.99"));
        secondProduct.setCategory(sampleCategory);
        secondProduct.setSupplier(sampleSupplier);

        productList = Arrays.asList(sampleProduct, secondProduct);
    }

    @Test
    @DisplayName("GET /products should return 200 and populate model with products")
    void test_listProducts() throws Exception {
        when(productService.findAll()).thenReturn(productList);
        when(categoryService.findAll()).thenReturn(Collections.singletonList(sampleCategory));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", hasSize(2)))
                .andExpect(model().attribute("products", hasItem(
                        hasProperty("name", is("Wireless Mouse"))
                )))
                .andExpect(model().attribute("products", hasItem(
                        hasProperty("name", is("Mechanical Keyboard"))
                )));

        verify(productService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /products/{id} should return 200 and display product detail")
    void test_showProductDetail() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleProduct);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product",
                        hasProperty("name", is("Wireless Mouse"))))
                .andExpect(model().attribute("product",
                        hasProperty("sku", is("ELEC-001"))))
                .andExpect(model().attribute("product",
                        hasProperty("price", is(new BigDecimal("29.99")))));

        verify(productService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /products/new should return 200 and display create form")
    void test_showCreateForm() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.singletonList(sampleCategory));
        when(supplierService.findAll()).thenReturn(Collections.singletonList(sampleSupplier));

        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("suppliers"))
                .andExpect(model().attribute("categories", hasSize(1)))
                .andExpect(model().attribute("suppliers", hasSize(1)));

        verify(categoryService, times(1)).findAll();
        verify(supplierService, times(1)).findAll();
    }
}
