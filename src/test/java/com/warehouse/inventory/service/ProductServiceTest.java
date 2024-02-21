package com.warehouse.inventory.service;

import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.model.Category;
import com.warehouse.inventory.model.Product;
import com.warehouse.inventory.model.Supplier;
import com.warehouse.inventory.repository.CategoryRepository;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private Category sampleCategory;
    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Electronics");

        sampleSupplier = new Supplier();
        sampleSupplier.setId(1L);
        sampleSupplier.setName("Acme Corp");
        sampleSupplier.setEmail("contact@acmecorp.com");

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setSku("ELEC-001");
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setDescription("Ergonomic wireless mouse with USB receiver");
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setMinStockLevel(10);
        sampleProduct.setCategory(sampleCategory);
        sampleProduct.setSupplier(sampleSupplier);
    }

    @Test
    @DisplayName("findAll should return a list of all products")
    void test_findAll() {
        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setSku("ELEC-002");
        secondProduct.setName("Mechanical Keyboard");
        secondProduct.setPrice(new BigDecimal("79.99"));

        List<Product> expectedProducts = Arrays.asList(sampleProduct, secondProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        List<Product> actualProducts = productService.findAll();

        assertThat(actualProducts).hasSize(2);
        assertThat(actualProducts).containsExactlyElementsOf(expectedProducts);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById should return product when it exists")
    void test_findById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product found = productService.findById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getSku()).isEqualTo("ELEC-001");
        assertThat(found.getName()).isEqualTo("Wireless Mouse");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById should throw ResourceNotFoundException when product does not exist")
    void test_findById_notFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("999");

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("save should persist and return the product")
    void test_save() {
        Product newProduct = new Product();
        newProduct.setSku("ELEC-003");
        newProduct.setName("USB-C Hub");
        newProduct.setPrice(new BigDecimal("49.99"));

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        savedProduct.setSku("ELEC-003");
        savedProduct.setName("USB-C Hub");
        savedProduct.setPrice(new BigDecimal("49.99"));

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.save(newProduct);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getSku()).isEqualTo("ELEC-003");
        assertThat(result.getName()).isEqualTo("USB-C Hub");
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("findLowStock should return products below minimum stock level")
    void test_findLowStock() {
        Product lowStockProduct1 = new Product();
        lowStockProduct1.setId(1L);
        lowStockProduct1.setName("Wireless Mouse");
        lowStockProduct1.setMinStockLevel(10);

        Product lowStockProduct2 = new Product();
        lowStockProduct2.setId(2L);
        lowStockProduct2.setName("Keyboard");
        lowStockProduct2.setMinStockLevel(5);

        List<Product> lowStockProducts = Arrays.asList(lowStockProduct1, lowStockProduct2);
        when(productRepository.findByCurrentStockLessThanMinStockLevel()).thenReturn(lowStockProducts);

        List<Product> result = productService.findLowStock();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .containsExactly("Wireless Mouse", "Keyboard");
        verify(productRepository, times(1)).findByCurrentStockLessThanMinStockLevel();
    }
}
