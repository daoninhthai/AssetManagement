package com.warehouse.inventory.config;

import com.warehouse.inventory.entity.Alert;
import com.warehouse.inventory.entity.Category;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.entity.Supplier;
import com.warehouse.inventory.entity.User;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.entity.WarehouseStock;
import com.warehouse.inventory.enums.AlertSeverity;
import com.warehouse.inventory.enums.AlertType;
import com.warehouse.inventory.enums.MovementType;
import com.warehouse.inventory.enums.UserRole;
import com.warehouse.inventory.enums.WarehouseType;
import com.warehouse.inventory.repository.AlertRepository;
import com.warehouse.inventory.repository.CategoryRepository;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.StockMovementRepository;
import com.warehouse.inventory.repository.SupplierRepository;
import com.warehouse.inventory.repository.UserRepository;
import com.warehouse.inventory.repository.WarehouseRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import com.warehouse.inventory.util.SkuGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("Starting database seeding...");

        List<Category> categories = seedCategories();
        List<Supplier> suppliers = seedSuppliers();
        List<Warehouse> warehouses = seedWarehouses();
        List<Product> products = seedProducts(categories, suppliers);
        seedWarehouseStock(products, warehouses);
        seedStockMovements(products, warehouses);
        seedAlerts(products, warehouses);
        seedUsers();

        log.info("Database seeding completed successfully!");
    }

    private List<Category> seedCategories() {
        log.info("Seeding categories...");
        String[][] categoryData = {
                {"Điện tử", "Các thiết bị và linh kiện điện tử"},
                {"Thực phẩm", "Thực phẩm và đồ uống các loại"},
                {"Văn phòng phẩm", "Dụng cụ và vật tư văn phòng"},
                {"Hóa chất", "Hóa chất công nghiệp và phòng thí nghiệm"},
                {"Máy móc", "Máy móc và thiết bị công nghiệp"},
                {"Nội thất", "Đồ nội thất văn phòng và gia đình"},
                {"Y tế", "Vật tư và thiết bị y tế"},
                {"Quần áo", "Quần áo bảo hộ và đồng phục"},
                {"Nguyên liệu", "Nguyên liệu thô và bán thành phẩm"},
                {"Đóng gói", "Vật liệu và dụng cụ đóng gói"}
        };

        List<Category> categories = new ArrayList<>();
        for (String[] data : categoryData) {
            Category cat = Category.builder()
                    .name(data[0])
                    .description(data[1])
                    .build();
            categories.add(categoryRepository.save(cat));
        }
        log.info("Seeded {} categories", categories.size());
        return categories;
    }

    private List<Supplier> seedSuppliers() {
        log.info("Seeding suppliers...");
        String[][] supplierData = {
                {"Công ty TNHH Phát Đạt", "Nguyễn Văn An", "phatdat@email.com", "0901234567", "123 Nguyễn Huệ, Q.1, TP.HCM"},
                {"CTCP Thương mại Hoàng Long", "Trần Thị Bích", "hoanglong@email.com", "0912345678", "45 Lê Lợi, Q.3, TP.HCM"},
                {"Công ty Minh Quang Electronics", "Phạm Minh Quang", "minhquang@email.com", "0923456789", "78 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM"},
                {"TNHH Thực phẩm Sài Gòn", "Lê Hoàng Nam", "saigonfood@email.com", "0934567890", "12 Phan Xích Long, Q.Phú Nhuận, TP.HCM"},
                {"Công ty Văn phòng phẩm Thiên Long", "Võ Thị Hoa", "thienlong@email.com", "0945678901", "56 Cách Mạng Tháng 8, Q.10, TP.HCM"},
                {"CTCP Hóa chất Đông Á", "Đặng Quốc Việt", "dongahc@email.com", "0956789012", "200 Quốc lộ 1, Bình Dương"},
                {"Công ty Máy móc Đại Phong", "Hồ Thanh Tùng", "daiphong@email.com", "0967890123", "345 Kha Vạn Cân, Q.Thủ Đức, TP.HCM"},
                {"TNHH Nội thất Hòa Phát", "Bùi Văn Đức", "hoaphat@email.com", "0978901234", "89 Nguyễn Thị Minh Khai, Q.1, TP.HCM"},
                {"Công ty Y tế Bảo Minh", "Ngô Thị Lan", "baominh@email.com", "0989012345", "67 Hai Bà Trưng, Q.1, TP.HCM"},
                {"CTCP Dệt may Việt Tiến", "Trương Quốc Bảo", "viettien@email.com", "0990123456", "15 Lý Thường Kiệt, Q.Tân Bình, TP.HCM"},
                {"Công ty Nguyên liệu Thành Công", "Phan Thị Mai", "thanhcong@email.com", "0901122334", "78 An Dương Vương, Q.5, TP.HCM"},
                {"TNHH Bao bì Tân Tiến", "Lý Minh Tuấn", "tantien@email.com", "0912233445", "234 Trường Chinh, Q.12, TP.HCM"},
                {"Công ty Kim khí Thăng Long", "Đỗ Hoàng Sơn", "thanglong@email.com", "0923344556", "12 Phạm Hùng, Hà Nội"},
                {"CTCP Điện máy Nguyễn Kim", "Nguyễn Thu Hà", "nguyenkim@email.com", "0934455667", "63 Trần Hưng Đạo, Q.1, TP.HCM"},
                {"Công ty Phân bón Cà Mau", "Trần Văn Tài", "pbcamau@email.com", "0945566778", "12 Hùng Vương, Cà Mau"},
                {"TNHH Dược phẩm OPC", "Lê Thị Hằng", "opc@email.com", "0956677889", "1017 Hồng Bàng, Q.6, TP.HCM"},
                {"Công ty Nhựa Đại Đồng Tiến", "Phạm Hữu Lộc", "daidongtien@email.com", "0967788990", "45 KCN Biên Hòa, Đồng Nai"},
                {"CTCP Gỗ Trường Thành", "Vương Thị Oanh", "truongthanh@email.com", "0978899001", "89 Quốc lộ 51, Bà Rịa"},
                {"Công ty Giấy Sài Gòn", "Hoàng Minh Trí", "giaysg@email.com", "0989900112", "123 KCN Tân Bình, TP.HCM"},
                {"TNHH Thiết bị An Phát", "Cao Thị Ngọc", "anphat@email.com", "0990011223", "56 Nguyễn Văn Trỗi, Q.Phú Nhuận, TP.HCM"}
        };

        List<Supplier> suppliers = new ArrayList<>();
        for (String[] data : supplierData) {
            Supplier supplier = Supplier.builder()
                    .name(data[0])
                    .contactPerson(data[1])
                    .email(data[2])
                    .phone(data[3])
                    .address(data[4])
                    .rating(3.5 + random.nextDouble() * 1.5)
                    .active(true)
                    .build();
            suppliers.add(supplierRepository.save(supplier));
        }
        log.info("Seeded {} suppliers", suppliers.size());
        return suppliers;
    }

    private List<Warehouse> seedWarehouses() {
        log.info("Seeding warehouses...");
        Object[][] warehouseData = {
                {"WH-HCM01", "Kho Trung tâm HCM", "Quận 7, TP. Hồ Chí Minh", WarehouseType.MAIN, 50000},
                {"WH-HN01", "Kho Hà Nội", "Long Biên, Hà Nội", WarehouseType.BRANCH, 30000},
                {"WH-DN01", "Kho Đà Nẵng", "Liên Chiểu, Đà Nẵng", WarehouseType.BRANCH, 20000},
                {"WH-BD01", "Kho Lạnh Bình Dương", "Dĩ An, Bình Dương", WarehouseType.COLD_STORAGE, 15000},
                {"WH-TD01", "Kho Phụ Thủ Đức", "TP. Thủ Đức, TP. HCM", WarehouseType.BRANCH, 25000}
        };

        List<Warehouse> warehouses = new ArrayList<>();
        for (Object[] data : warehouseData) {
            Warehouse wh = Warehouse.builder()
                    .code((String) data[0])
                    .name((String) data[1])
                    .address((String) data[2])
                    .type((WarehouseType) data[3])
                    .capacity((Integer) data[4])
                    .currentOccupancy(0)
                    .active(true)
                    .build();
            warehouses.add(warehouseRepository.save(wh));
        }
        log.info("Seeded {} warehouses", warehouses.size());
        return warehouses;
    }

    private List<Product> seedProducts(List<Category> categories, List<Supplier> suppliers) {
        log.info("Seeding products...");

        Map<String, List<String[]>> productsByCategory = new HashMap<>();

        // Dien tu - 20 products
        productsByCategory.put("Điện tử", Arrays.asList(
                new String[]{"Laptop Dell Vostro 3520", "Cái", "15500000", "14000000"},
                new String[]{"Màn hình Samsung 24 inch", "Cái", "4200000", "3800000"},
                new String[]{"Chuột Logitech M331", "Cái", "350000", "280000"},
                new String[]{"Bàn phím cơ Akko 3068", "Cái", "1200000", "950000"},
                new String[]{"Tai nghe Sony WH-1000XM4", "Cái", "6500000", "5800000"},
                new String[]{"USB Kingston 64GB", "Cái", "150000", "120000"},
                new String[]{"Ổ cứng SSD Samsung 500GB", "Cái", "1800000", "1500000"},
                new String[]{"Pin sạc dự phòng Anker 10000mAh", "Cái", "550000", "420000"},
                new String[]{"Cáp HDMI 2.0 dài 3m", "Sợi", "120000", "85000"},
                new String[]{"Webcam Logitech C920", "Cái", "2200000", "1800000"},
                new String[]{"Máy in HP LaserJet Pro", "Cái", "5500000", "4800000"},
                new String[]{"Router Wifi TP-Link AX1500", "Cái", "890000", "720000"},
                new String[]{"Switch mạng 8 port Cisco", "Cái", "1500000", "1200000"},
                new String[]{"Loa Bluetooth JBL Flip 5", "Cái", "2100000", "1750000"},
                new String[]{"Máy chiếu Epson EB-X51", "Cái", "9800000", "8500000"},
                new String[]{"RAM DDR4 8GB Kingston", "Thanh", "650000", "520000"},
                new String[]{"Dây mạng Cat6 100m", "Cuộn", "450000", "350000"},
                new String[]{"Bộ chia USB Hub 4 port", "Cái", "180000", "130000"},
                new String[]{"Quạt tản nhiệt laptop", "Cái", "280000", "200000"},
                new String[]{"Ổ cắm điện thông minh", "Cái", "350000", "260000"}
        ));

        // Thuc pham - 20 products
        productsByCategory.put("Thực phẩm", Arrays.asList(
                new String[]{"Gạo ST25 túi 5kg", "Túi", "125000", "95000"},
                new String[]{"Nước mắm Phú Quốc 500ml", "Chai", "65000", "48000"},
                new String[]{"Dầu ăn Tường An 1L", "Chai", "42000", "35000"},
                new String[]{"Đường Biên Hòa 1kg", "Gói", "22000", "18000"},
                new String[]{"Bột mì đa dụng 1kg", "Gói", "18000", "14000"},
                new String[]{"Mì gói Hảo Hảo thùng 30", "Thùng", "95000", "78000"},
                new String[]{"Nước tương Maggi 700ml", "Chai", "28000", "22000"},
                new String[]{"Cà phê G7 hộp 18 gói", "Hộp", "55000", "42000"},
                new String[]{"Trà Lipton hộp 100 gói", "Hộp", "85000", "68000"},
                new String[]{"Sữa đặc Ông Thọ 380g", "Hộp", "18000", "14500"},
                new String[]{"Nước ngọt Coca Cola thùng 24", "Thùng", "185000", "155000"},
                new String[]{"Bia Tiger thùng 24 lon", "Thùng", "295000", "250000"},
                new String[]{"Dầu hào Lee Kum Kee 510g", "Chai", "58000", "45000"},
                new String[]{"Hạt nêm Knorr 900g", "Gói", "52000", "40000"},
                new String[]{"Bơ thực vật Tường An 200g", "Hộp", "25000", "19000"},
                new String[]{"Nước mắm Nam Ngư 500ml", "Chai", "32000", "25000"},
                new String[]{"Bánh quy AFC hộp 200g", "Hộp", "28000", "21000"},
                new String[]{"Sữa tươi Vinamilk 1L", "Hộp", "32000", "26000"},
                new String[]{"Giấm ăn Mekong 500ml", "Chai", "12000", "9000"},
                new String[]{"Tương ớt Cholimex 520g", "Chai", "22000", "17000"}
        ));

        // Van phong pham - 20 products
        productsByCategory.put("Văn phòng phẩm", Arrays.asList(
                new String[]{"Giấy A4 Double A 500 tờ", "Ream", "78000", "62000"},
                new String[]{"Bút bi Thiên Long TL-023", "Cây", "3500", "2500"},
                new String[]{"Bút đánh dấu Stabilo Boss", "Cây", "18000", "13000"},
                new String[]{"Sổ tay A5 bìa cứng 200 trang", "Cuốn", "35000", "25000"},
                new String[]{"Kẹp giấy binder 32mm hộp", "Hộp", "12000", "8500"},
                new String[]{"Ghim bấm Kangaro No.10", "Hộp", "5500", "4000"},
                new String[]{"Bìa lá A4 trong suốt", "Cái", "2000", "1400"},
                new String[]{"Băng keo trong 48mm", "Cuộn", "15000", "11000"},
                new String[]{"Kéo văn phòng Deli 170mm", "Cây", "25000", "18000"},
                new String[]{"Bút xóa kéo Plus", "Cây", "22000", "16000"},
                new String[]{"Mực in HP 680 đen", "Hộp", "185000", "145000"},
                new String[]{"Giấy note 3x3 inch 400 tờ", "Xấp", "18000", "13000"},
                new String[]{"File hồ sơ nhựa A4", "Cái", "8000", "5500"},
                new String[]{"Thước kẻ nhựa 30cm", "Cây", "5000", "3500"},
                new String[]{"Dao rọc giấy lớn", "Cây", "15000", "10000"},
                new String[]{"Hộp bút để bàn đa năng", "Cái", "45000", "32000"},
                new String[]{"Giấy than A4 hộp 100 tờ", "Hộp", "35000", "25000"},
                new String[]{"Bìa cứng đóng sách A4", "Tấm", "4000", "2800"},
                new String[]{"Phong bì thư A4", "Cái", "1500", "1000"},
                new String[]{"Bảng white board 60x90cm", "Cái", "250000", "190000"}
        ));

        // Hoa chat - 20 products
        productsByCategory.put("Hóa chất", Arrays.asList(
                new String[]{"Cồn y tế 70 độ 500ml", "Chai", "28000", "20000"},
                new String[]{"Nước rửa tay Lifebuoy 500ml", "Chai", "65000", "50000"},
                new String[]{"Nước tẩy Javel 1L", "Chai", "15000", "11000"},
                new String[]{"Dung dịch sát khuẩn 5L", "Can", "180000", "140000"},
                new String[]{"Axit sulfuric H2SO4 1L", "Chai", "45000", "35000"},
                new String[]{"Natri hydroxit NaOH 1kg", "Gói", "38000", "28000"},
                new String[]{"Ethanol tinh khiết 99% 1L", "Chai", "85000", "65000"},
                new String[]{"Dung môi Acetone 1L", "Chai", "55000", "42000"},
                new String[]{"Xà phòng công nghiệp 5L", "Can", "120000", "90000"},
                new String[]{"Chất tẩy rửa đa năng 1L", "Chai", "35000", "25000"},
                new String[]{"Keo dán epoxy 2 thành phần", "Bộ", "75000", "55000"},
                new String[]{"Dầu bôi trơn WD-40 400ml", "Chai", "95000", "72000"},
                new String[]{"Silicon chống thấm 300ml", "Tuýp", "45000", "32000"},
                new String[]{"Mỡ bò công nghiệp 1kg", "Hộp", "65000", "48000"},
                new String[]{"Axit clohidric HCl 1L", "Chai", "32000", "24000"},
                new String[]{"Nước cất tinh khiết 5L", "Can", "25000", "18000"},
                new String[]{"Bột giặt công nghiệp 25kg", "Bao", "350000", "280000"},
                new String[]{"Thuốc tẩy oxy già 500ml", "Chai", "18000", "13000"},
                new String[]{"Keo 502 siêu dính 3g", "Tuýp", "8000", "5500"},
                new String[]{"Dung dịch làm sạch kính 500ml", "Chai", "28000", "20000"}
        ));

        // May moc - 20 products
        productsByCategory.put("Máy móc", Arrays.asList(
                new String[]{"Máy khoan Bosch GSB 13RE", "Cái", "1350000", "1100000"},
                new String[]{"Máy mài góc Makita 100mm", "Cái", "980000", "780000"},
                new String[]{"Máy cắt sắt Dewalt 355mm", "Cái", "2800000", "2300000"},
                new String[]{"Máy hàn điện tử Jasic 250A", "Cái", "3200000", "2700000"},
                new String[]{"Máy nén khí Puma 50L", "Cái", "4500000", "3800000"},
                new String[]{"Máy bơm nước Pentax 1HP", "Cái", "2100000", "1750000"},
                new String[]{"Máy phát điện Honda 3KVA", "Cái", "15000000", "12500000"},
                new String[]{"Máy cưa đĩa Bosch GKS 190", "Cái", "2500000", "2050000"},
                new String[]{"Thang nhôm rút gọn 4m", "Cái", "1800000", "1400000"},
                new String[]{"Máy đo laser Bosch GLM 50C", "Cái", "2800000", "2300000"},
                new String[]{"Bộ dụng cụ sửa chữa 120 chi tiết", "Bộ", "850000", "650000"},
                new String[]{"Kìm bấm cos đa năng", "Cái", "250000", "180000"},
                new String[]{"Mỏ lết Stanley 12 inch", "Cái", "185000", "140000"},
                new String[]{"Tua vít bộ 8 cây Stanley", "Bộ", "120000", "85000"},
                new String[]{"Cưa tay Irwin 450mm", "Cái", "180000", "135000"},
                new String[]{"Búa đinh Stanley 500g", "Cái", "95000", "70000"},
                new String[]{"Thước cuộn 5m Stanley", "Cái", "65000", "48000"},
                new String[]{"Máy vặn vít pin Makita 12V", "Cái", "1600000", "1300000"},
                new String[]{"Đèn pin LED Fenix 1000lm", "Cái", "450000", "350000"},
                new String[]{"Máy hút bụi công nghiệp 30L", "Cái", "3500000", "2900000"}
        ));

        // Noi that - 20 products
        productsByCategory.put("Nội thất", Arrays.asList(
                new String[]{"Bàn làm việc gỗ MDF 120x60cm", "Cái", "1500000", "1100000"},
                new String[]{"Ghế xoay văn phòng lưng cao", "Cái", "1800000", "1350000"},
                new String[]{"Tủ hồ sơ sắt 4 ngăn", "Cái", "2500000", "2000000"},
                new String[]{"Kệ sách gỗ 5 tầng", "Cái", "1200000", "900000"},
                new String[]{"Bàn họp chữ nhật 240x120cm", "Cái", "4500000", "3600000"},
                new String[]{"Ghế phòng họp có tay vịn", "Cái", "650000", "480000"},
                new String[]{"Tủ tài liệu kính 2 cánh", "Cái", "3200000", "2500000"},
                new String[]{"Vách ngăn văn phòng 120x150cm", "Tấm", "800000", "600000"},
                new String[]{"Bàn tiếp khách oval", "Cái", "2200000", "1700000"},
                new String[]{"Sofa đơn văn phòng", "Cái", "3500000", "2800000"},
                new String[]{"Kệ để máy in di động", "Cái", "450000", "320000"},
                new String[]{"Tủ locker sắt 6 ngăn", "Cái", "2800000", "2200000"},
                new String[]{"Bàn quầy lễ tân cong", "Cái", "5500000", "4200000"},
                new String[]{"Giá treo bảng di động", "Cái", "650000", "480000"},
                new String[]{"Rèm cuốn văn phòng 150x200cm", "Bộ", "350000", "250000"},
                new String[]{"Thảm trải sàn văn phòng 50x50cm", "Tấm", "45000", "32000"},
                new String[]{"Đèn bàn LED chống cận", "Cái", "280000", "200000"},
                new String[]{"Quạt trần công nghiệp 1.4m", "Cái", "1200000", "900000"},
                new String[]{"Máy lạnh Daikin 1.5HP", "Cái", "12000000", "10000000"},
                new String[]{"Bình nước nóng lạnh Kangaroo", "Cái", "2800000", "2200000"}
        ));

        // Y te - 20 products
        productsByCategory.put("Y tế", Arrays.asList(
                new String[]{"Khẩu trang y tế 4 lớp hộp 50", "Hộp", "65000", "48000"},
                new String[]{"Găng tay y tế nitrile hộp 100", "Hộp", "120000", "90000"},
                new String[]{"Nhiệt kế điện tử Omron", "Cái", "250000", "190000"},
                new String[]{"Máy đo huyết áp Omron", "Cái", "950000", "750000"},
                new String[]{"Máy đo SpO2 Jumper", "Cái", "350000", "270000"},
                new String[]{"Bông băng y tế cuộn 10cm", "Cuộn", "15000", "10000"},
                new String[]{"Cồn sát khuẩn 90 độ 500ml", "Chai", "25000", "18000"},
                new String[]{"Băng cá nhân Urgo hộp 100", "Hộp", "55000", "40000"},
                new String[]{"Nước muối sinh lý 500ml", "Chai", "12000", "8500"},
                new String[]{"Bơm tiêm 5ml hộp 100", "Hộp", "85000", "65000"},
                new String[]{"Áo blouse trắng size L", "Cái", "120000", "85000"},
                new String[]{"Kính bảo hộ y tế", "Cái", "45000", "32000"},
                new String[]{"Tấm chắn face shield", "Cái", "25000", "18000"},
                new String[]{"Bộ sơ cứu First Aid Kit", "Bộ", "350000", "260000"},
                new String[]{"Nẹp cố định tay chân", "Bộ", "85000", "62000"},
                new String[]{"Xe lăn inox tiêu chuẩn", "Cái", "2500000", "2000000"},
                new String[]{"Nạng nhôm điều chỉnh", "Đôi", "350000", "260000"},
                new String[]{"Khẩu trang N95 3M hộp 20", "Hộp", "280000", "210000"},
                new String[]{"Bộ test nhanh Covid hộp 25", "Hộp", "450000", "340000"},
                new String[]{"Máy xông mũi họng Omron", "Cái", "1200000", "950000"}
        ));

        // Quan ao - 20 products
        productsByCategory.put("Quần áo", Arrays.asList(
                new String[]{"Áo bảo hộ lao động xanh", "Cái", "120000", "85000"},
                new String[]{"Quần bảo hộ lao động", "Cái", "95000", "68000"},
                new String[]{"Mũ bảo hộ nhựa ABS", "Cái", "55000", "38000"},
                new String[]{"Giày bảo hộ Steel toe", "Đôi", "350000", "260000"},
                new String[]{"Găng tay vải bảo hộ", "Đôi", "15000", "10000"},
                new String[]{"Áo phản quang giao thông", "Cái", "45000", "32000"},
                new String[]{"Kính bảo hộ chống bụi", "Cái", "35000", "24000"},
                new String[]{"Khẩu trang vải kháng khuẩn", "Cái", "18000", "12000"},
                new String[]{"Bao tai chống ồn 3M", "Cái", "180000", "135000"},
                new String[]{"Áo mưa bộ vải dù", "Bộ", "85000", "60000"},
                new String[]{"Đồng phục công sở nam", "Bộ", "450000", "340000"},
                new String[]{"Đồng phục công sở nữ", "Bộ", "480000", "360000"},
                new String[]{"Áo thun đồng phục polo", "Cái", "85000", "60000"},
                new String[]{"Nón bảo hiểm 3/4 đầu", "Cái", "250000", "180000"},
                new String[]{"Dây đai an toàn toàn thân", "Bộ", "650000", "480000"},
                new String[]{"Ủng cao su chống hóa chất", "Đôi", "120000", "85000"},
                new String[]{"Áo khoác gió nhẹ", "Cái", "180000", "130000"},
                new String[]{"Tạp dề chống thấm", "Cái", "45000", "32000"},
                new String[]{"Bao tay cao su dài", "Đôi", "28000", "19000"},
                new String[]{"Mặt nạ phòng độc 3M", "Cái", "450000", "340000"}
        ));

        // Nguyen lieu - 20 products
        productsByCategory.put("Nguyên liệu", Arrays.asList(
                new String[]{"Thép tấm SS400 2mm 1220x2440", "Tấm", "850000", "680000"},
                new String[]{"Ống thép mạ kẽm D42", "Cây", "185000", "145000"},
                new String[]{"Nhôm thanh định hình 6063", "Cây", "250000", "195000"},
                new String[]{"Tôn lạnh 0.4mm x 1.2m", "Mét", "55000", "42000"},
                new String[]{"Gỗ ván MDF 18mm 1220x2440", "Tấm", "380000", "300000"},
                new String[]{"Xi măng Holcim bao 50kg", "Bao", "95000", "78000"},
                new String[]{"Cát xây dựng mịn 1m3", "m3", "250000", "200000"},
                new String[]{"Đá 1x2 xây dựng 1m3", "m3", "300000", "240000"},
                new String[]{"Gạch ống 4 lỗ 8x8x18cm", "Viên", "1200", "900"},
                new String[]{"Sắt phi 10 cuộn 2 tấn", "Cuộn", "18500000", "16000000"},
                new String[]{"Dây đồng điện 2.5mm cuộn 100m", "Cuộn", "850000", "680000"},
                new String[]{"Ống nhựa PVC D60 dài 4m", "Cây", "45000", "34000"},
                new String[]{"Keo silicon gốc nước 300ml", "Tuýp", "35000", "25000"},
                new String[]{"Sơn nước nội thất 5L", "Thùng", "250000", "190000"},
                new String[]{"Sơn chống rỉ đỏ 800ml", "Lon", "65000", "48000"},
                new String[]{"Bu lông M10x50 inox hộp 100", "Hộp", "120000", "88000"},
                new String[]{"Đinh vít gỗ 4x40mm hộp 500", "Hộp", "45000", "32000"},
                new String[]{"Băng keo cách điện 3M cuộn", "Cuộn", "18000", "13000"},
                new String[]{"Dây rút nhựa 300mm bịch 100", "Bịch", "25000", "18000"},
                new String[]{"Lưới thép hàn D4 ô 100x100", "Tấm", "185000", "145000"}
        ));

        // Dong goi - 20 products
        productsByCategory.put("Đóng gói", Arrays.asList(
                new String[]{"Thùng carton 3 lớp 40x30x25cm", "Cái", "8500", "6000"},
                new String[]{"Thùng carton 5 lớp 60x40x40cm", "Cái", "18000", "13000"},
                new String[]{"Túi nilon PE 25x35cm kg", "Kg", "45000", "34000"},
                new String[]{"Màng co PE cuộn 50cm", "Cuộn", "85000", "65000"},
                new String[]{"Xốp hơi chống sốc cuộn 1.2m", "Mét", "12000", "8500"},
                new String[]{"Băng keo đóng hàng 48mm x 100m", "Cuộn", "18000", "13000"},
                new String[]{"Dây đai nhựa PP 15mm cuộn", "Cuộn", "120000", "90000"},
                new String[]{"Khóa đai nhựa PP hộp 1000", "Hộp", "85000", "62000"},
                new String[]{"Pallet nhựa 1100x1100mm", "Cái", "350000", "270000"},
                new String[]{"Pallet gỗ 1200x1000mm", "Cái", "180000", "135000"},
                new String[]{"Túi zip lock 20x30cm bịch 100", "Bịch", "35000", "25000"},
                new String[]{"Hộp xốp EPS 30x20x15cm", "Cái", "12000", "8500"},
                new String[]{"Giấy gói hàng kraft cuộn", "Cuộn", "65000", "48000"},
                new String[]{"Nhãn dán mã vạch cuộn 1000", "Cuộn", "45000", "32000"},
                new String[]{"Máy bắn ghim đóng hàng", "Cái", "250000", "185000"},
                new String[]{"Ghim đóng thùng hộp 5000", "Hộp", "35000", "25000"},
                new String[]{"Túi giấy kraft có quai 25x30", "Cái", "3500", "2500"},
                new String[]{"Màng PE stretch tay 50cm", "Cuộn", "55000", "40000"},
                new String[]{"Hạt chống ẩm silica gel 1kg", "Gói", "45000", "32000"},
                new String[]{"Tem niêm phong seal hộp 500", "Hộp", "65000", "48000"}
        ));

        List<Product> allProducts = new ArrayList<>();
        String[] prefixes = {"DT", "TP", "VP", "HC", "MM", "NT", "YT", "QA", "NL", "DG"};
        int prefixIdx = 0;

        for (Category category : categories) {
            List<String[]> productList = productsByCategory.get(category.getName());
            if (productList == null) continue;

            String prefix = prefixes[prefixIdx++];
            Supplier supplier = suppliers.get(random.nextInt(suppliers.size()));

            for (String[] pData : productList) {
                int minStock = 10 + random.nextInt(40);
                int maxStock = minStock * 5 + random.nextInt(200);
                int reorderPoint = minStock + random.nextInt(20);

                Product product = Product.builder()
                        .name(pData[0])
                        .sku(SkuGenerator.generate(prefix))
                        .description("Sản phẩm " + pData[0] + " chất lượng cao")
                        .category(category)
                        .supplier(suppliers.get(random.nextInt(suppliers.size())))
                        .unit(pData[1])
                        .unitPrice(new BigDecimal(pData[2]))
                        .costPrice(new BigDecimal(pData[3]))
                        .minStockLevel(minStock)
                        .maxStockLevel(maxStock)
                        .reorderPoint(reorderPoint)
                        .active(true)
                        .build();

                allProducts.add(productRepository.save(product));
            }
        }

        log.info("Seeded {} products", allProducts.size());
        return allProducts;
    }

    private void seedWarehouseStock(List<Product> products, List<Warehouse> warehouses) {
        log.info("Seeding warehouse stock...");
        int stockCount = 0;

        for (Product product : products) {
            // Each product in 1-3 random warehouses
            int numWarehouses = 1 + random.nextInt(3);
            List<Warehouse> selectedWarehouses = new ArrayList<>();

            for (int i = 0; i < numWarehouses && i < warehouses.size(); i++) {
                Warehouse wh;
                do {
                    wh = warehouses.get(random.nextInt(warehouses.size()));
                } while (selectedWarehouses.contains(wh));
                selectedWarehouses.add(wh);

                int quantity = product.getMinStockLevel() + random.nextInt(
                        product.getMaxStockLevel() - product.getMinStockLevel() + 1);

                // Some products should have low stock for realism
                if (random.nextDouble() < 0.15) {
                    quantity = random.nextInt(Math.max(1, product.getMinStockLevel()));
                }

                WarehouseStock stock = WarehouseStock.builder()
                        .warehouse(wh)
                        .product(product)
                        .quantity(quantity)
                        .build();
                warehouseStockRepository.save(stock);
                stockCount++;
            }
        }
        log.info("Seeded {} warehouse stock records", stockCount);
    }

    private void seedStockMovements(List<Product> products, List<Warehouse> warehouses) {
        log.info("Seeding stock movements (5000)...");
        MovementType[] types = {MovementType.IN, MovementType.OUT, MovementType.IN, MovementType.OUT,
                MovementType.TRANSFER, MovementType.IN, MovementType.OUT, MovementType.ADJUSTMENT};
        String[] reasons = {
                "Nhập hàng từ nhà cung cấp", "Xuất hàng cho khách", "Bổ sung tồn kho",
                "Xuất hàng theo đơn", "Chuyển kho nội bộ", "Điều chỉnh kiểm kê",
                "Nhập hàng trả lại", "Xuất hàng khuyến mãi", "Nhập hàng bổ sung",
                "Xuất hàng sản xuất"
        };

        List<StockMovement> movements = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5000; i++) {
            Product product = products.get(random.nextInt(products.size()));
            MovementType type = types[random.nextInt(types.length)];

            Warehouse fromWarehouse = null;
            Warehouse toWarehouse = null;

            switch (type) {
                case IN:
                    toWarehouse = warehouses.get(random.nextInt(warehouses.size()));
                    break;
                case OUT:
                    fromWarehouse = warehouses.get(random.nextInt(warehouses.size()));
                    break;
                case TRANSFER:
                    fromWarehouse = warehouses.get(random.nextInt(warehouses.size()));
                    do {
                        toWarehouse = warehouses.get(random.nextInt(warehouses.size()));
                    } while (toWarehouse.getId().equals(fromWarehouse.getId()));
                    break;
                case ADJUSTMENT:
                    toWarehouse = warehouses.get(random.nextInt(warehouses.size()));
                    break;
            }

            int quantity = 1 + random.nextInt(100);
            int daysAgo = random.nextInt(90);
            int hoursAgo = random.nextInt(24);
            int minutesAgo = random.nextInt(60);
            LocalDateTime createdAt = now.minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo);

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .fromWarehouse(fromWarehouse)
                    .toWarehouse(toWarehouse)
                    .quantity(quantity)
                    .type(type)
                    .reason(reasons[random.nextInt(reasons.length)])
                    .reference("REF-" + String.format("%06d", i + 1))
                    .performedBy("system")
                    .build();

            StockMovement saved = stockMovementRepository.save(movement);

            // Manually set createdAt for historical data
            stockMovementRepository.save(saved);
        }

        log.info("Seeded 5000 stock movements");
    }

    private void seedAlerts(List<Product> products, List<Warehouse> warehouses) {
        log.info("Seeding alerts...");

        String[] messages = {
                "Tồn kho thấp dưới mức tối thiểu",
                "Cần bổ sung hàng gấp",
                "Sản phẩm sắp hết hàng",
                "Phát hiện bất thường trong xuất kho",
                "Tồn kho vượt mức tối đa"
        };

        AlertType[] alertTypes = {AlertType.LOW_STOCK, AlertType.LOW_STOCK,
                AlertType.LOW_STOCK, AlertType.ANOMALY, AlertType.OVERSTOCK};
        AlertSeverity[] severities = {AlertSeverity.HIGH, AlertSeverity.CRITICAL,
                AlertSeverity.MEDIUM, AlertSeverity.LOW, AlertSeverity.MEDIUM};

        for (int i = 0; i < 15; i++) {
            Product product = products.get(random.nextInt(products.size()));
            Warehouse warehouse = warehouses.get(random.nextInt(warehouses.size()));
            int idx = random.nextInt(messages.length);

            Alert alert = Alert.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .type(alertTypes[idx])
                    .message(String.format("%s - %s tại %s", messages[idx], product.getName(), warehouse.getName()))
                    .severity(severities[idx])
                    .resolved(i > 10)
                    .build();

            if (i > 10) {
                alert.setResolvedBy("admin");
                alert.setResolvedAt(LocalDateTime.now().minusDays(random.nextInt(10)));
            }

            alertRepository.save(alert);
        }
        log.info("Seeded 15 alerts");
    }

    private void seedUsers() {
        log.info("Seeding users...");

        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên")
                    .email("admin@warehouse.com")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("manager")) {
            User manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Quản lý kho")
                    .email("manager@warehouse.com")
                    .role(UserRole.MANAGER)
                    .active(true)
                    .build();
            userRepository.save(manager);
        }

        if (!userRepository.existsByUsername("staff")) {
            User staff = User.builder()
                    .username("staff")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Nhân viên kho")
                    .email("staff@warehouse.com")
                    .role(UserRole.STAFF)
                    .active(true)
                    .build();
            userRepository.save(staff);
        }

        log.info("Seeded users (admin/admin123, manager/manager123, staff/staff123)");
    }
}
