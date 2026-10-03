package com.cash.cash.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cash.cash.model.Cash;
import com.cash.cash.model.Coupon;
import com.cash.cash.repository.CashRepository;
import com.cash.cash.repository.CouponRepository;

@Controller
public class CashController {

    @Autowired
    private CashRepository cashRepository;

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping("/")
    public String mainPage() {
        return "index";
    }

    @GetMapping("/cash")
    public String listCash(Model model) {

        model.addAttribute(
                "cash",
                cashRepository.findAll()
        );

        model.addAttribute(
                "coupons",
                couponRepository.findAll()
        );

        return "list";
    }

    /*
     * ADD / EDIT ITEM PAGE
     */
    @GetMapping("/cash/new")
    public String newCash(Model model) {

        model.addAttribute(
                "cash",
                new Cash()
        );

        return "form";
    }

    /*
     * SAVE ITEM
     */
    @PostMapping("/cash")
    public String saveCash(
            @ModelAttribute Cash cash) {

        String code = cash.getCode().trim();

        /*
         * Don't allow a coupon to use
         * the same barcode as an item.
         */
        if (couponRepository
                .findByCode(code)
                .isPresent()) {

            return "redirect:/cash/new?error=conflict";
        }

        cash.setCode(code);

        /*
         * If name is blank,
         * save NULL to MySQL.
         */
        if (cash.getName() != null
                && cash.getName().trim().isEmpty()) {

            cash.setName(null);

        } else if (cash.getName() != null) {

            cash.setName(
                    cash.getName().trim()
            );
        }

        cashRepository.save(cash);

        return "redirect:/cash";
    }

    /*
     * EDIT ITEM
     */
    @GetMapping("/cash/edit/{id}")
    public String editCash(
            @PathVariable Long id,
            Model model) {

        Optional<Cash> cash =
                cashRepository.findById(id);

        if (cash.isPresent()) {

            model.addAttribute(
                    "cash",
                    cash.get()
            );

            return "form";
        }

        return "redirect:/cash";
    }

    /*
     * DELETE ITEM
     */
    @GetMapping("/cash/delete/{id}")
    public String deleteCash(
            @PathVariable Long id) {

        cashRepository.deleteById(id);

        return "redirect:/cash";
    }

    /*
     * SCANNER
     *
     * Checks coupons first.
     * If it isn't a coupon,
     * checks for an item.
     */
    @GetMapping("/cash/scan")
    @ResponseBody
    public ResponseEntity<?> scanCode(
            @RequestParam String code) {

        String cleanCode = code.trim();

        /*
         * Check coupon first.
         */
        Optional<Coupon> coupon =
                couponRepository.findByCode(
                        cleanCode
                );

        if (coupon.isPresent()) {

            Coupon c = coupon.get();

            return ResponseEntity.ok(
                    new ScanResponse(
                            "COUPON",
                            c.getCode(),
                            c.getDiscountPercent(),
                            c.getItemCode(),
                            0,
                            0,
                            ""
                    )
            );
        }

        /*
         * Check normal item.
         */
        Optional<Cash> cash =
                cashRepository.findByCode(
                        cleanCode
                );

        if (cash.isPresent()) {

            Cash item = cash.get();

            return ResponseEntity.ok(
                    new ScanResponse(
                            "ITEM",
                            item.getCode(),
                            0,
                            "",
                            item.getId(),
                            item.getPrice(),
                            item.getName()
                    )
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }

    /*
     * EDIT PRICE FROM POS
     */
    @PostMapping("/cash/update-price/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePrice(
            @PathVariable Long id,
            @RequestParam double price) {

        Optional<Cash> optionalCash =
                cashRepository.findById(id);

        if (optionalCash.isEmpty()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (price < 0) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Price cannot be negative."
                    );
        }

        Cash cash = optionalCash.get();

        cash.setPrice(price);

        cashRepository.save(cash);

        return ResponseEntity.ok(cash);
    }

    /*
     * Scanner response object.
     */
    public static class ScanResponse {

        private String type;

        private String code;

        private double discountPercent;

        private String itemCode;

        private Long id;

        private double price;

        private String name;

        public ScanResponse(
                String type,
                String code,
                double discountPercent,
                String itemCode,
                long id,
                double price,
                String name) {

            this.type = type;
            this.code = code;
            this.discountPercent =
                    discountPercent;
            this.itemCode = itemCode;
            this.id = id;
            this.price = price;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

        public double getDiscountPercent() {
            return discountPercent;
        }

        public String getItemCode() {
            return itemCode;
        }

        public Long getId() {
            return id;
        }

        public double getPrice() {
            return price;
        }

        public String getName() {
            return name;
        }
    }
}