package com.cash.cash.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cash.cash.model.Cash;
import com.cash.cash.model.Coupon;
import com.cash.cash.repository.CashRepository;
import com.cash.cash.repository.CouponRepository;

@Controller
public class CouponController {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CashRepository cashRepository;

    /*
     * ADD COUPON PAGE
     */
    @GetMapping("/coupons/new")
    public String newCoupon(Model model) {

        model.addAttribute(
                "coupon",
                new Coupon()
        );

        model.addAttribute(
                "cash",
                cashRepository.findAll()
        );

        return "coupon-form";
    }

    /*
     * SAVE COUPON
     */
    @PostMapping("/coupons")
    public String saveCoupon(
            @ModelAttribute Coupon coupon,
            @RequestParam String itemCode) {

        String code =
                coupon.getCode().trim();

        String item =
                itemCode.trim();

        if (code.isEmpty()) {

            return "redirect:/coupons/new?error=code";
        }

        if (item.isEmpty()) {

            return "redirect:/coupons/new?error=item";
        }

        if (coupon.getDiscountPercent() <= 0
                || coupon.getDiscountPercent() > 100) {

            return "redirect:/coupons/new?error=percent";
        }

        /*
         * Coupon code must be unique.
         */
        if (couponRepository
                .findByCode(code)
                .isPresent()) {

            return "redirect:/coupons/new?error=duplicate";
        }

        /*
         * Make sure the item exists.
         */
        Optional<Cash> itemExists =
                cashRepository.findByCode(item);

        if (itemExists.isEmpty()) {

            return "redirect:/coupons/new?error=itemnotfound";
        }

        /*
         * Coupon code cannot be
         * the same as an item code.
         */
        Optional<Cash> conflictingCash =
                cashRepository.findByCode(code);

        if (conflictingCash.isPresent()) {

            return "redirect:/coupons/new?error=conflict";
        }

        coupon.setCode(code);

        coupon.setItemCode(item);

        couponRepository.save(coupon);

        return "redirect:/cash";
    }

    /*
     * EDIT COUPON
     */
    @GetMapping("/coupons/edit/{id}")
    public String editCoupon(
            @PathVariable Long id,
            Model model) {

        Optional<Coupon> coupon =
                couponRepository.findById(id);

        if (coupon.isEmpty()) {

            return "redirect:/cash";
        }

        model.addAttribute(
                "coupon",
                coupon.get()
        );

        model.addAttribute(
                "cash",
                cashRepository.findAll()
        );

        return "coupon-form";
    }

    /*
     * UPDATE COUPON
     */
    @PostMapping("/coupons/update/{id}")
    public String updateCoupon(
            @PathVariable Long id,
            @ModelAttribute Coupon coupon,
            @RequestParam String itemCode) {

        Optional<Coupon> existing =
                couponRepository.findById(id);

        if (existing.isEmpty()) {

            return "redirect:/cash";
        }

        if (coupon.getDiscountPercent() <= 0
                || coupon.getDiscountPercent() > 100) {

            return "redirect:/coupons/edit/"
                    + id
                    + "?error=percent";
        }

        String code =
                coupon.getCode().trim();

        String item =
                itemCode.trim();

        Optional<Cash> itemExists =
                cashRepository.findByCode(item);

        if (itemExists.isEmpty()) {

            return "redirect:/coupons/edit/"
                    + id
                    + "?error=itemnotfound";
        }

        /*
         * Don't allow coupon barcode
         * to match an item barcode.
         */
        Optional<Cash> conflictingCash =
                cashRepository.findByCode(code);

        if (conflictingCash.isPresent()) {

            return "redirect:/coupons/edit/"
                    + id
                    + "?error=conflict";
        }

        /*
         * Don't allow another coupon
         * to have the same code.
         */
        Optional<Coupon> duplicate =
                couponRepository.findByCode(code);

        if (duplicate.isPresent()
                && !duplicate.get()
                        .getId()
                        .equals(id)) {

            return "redirect:/coupons/edit/"
                    + id
                    + "?error=duplicate";
        }

        Coupon updated =
                existing.get();

        updated.setCode(code);

        updated.setDiscountPercent(
                coupon.getDiscountPercent()
        );

        updated.setItemCode(item);

        couponRepository.save(updated);

        return "redirect:/cash";
    }

    /*
     * DELETE COUPON
     */
    @GetMapping("/coupons/delete/{id}")
    public String deleteCoupon(
            @PathVariable Long id) {

        couponRepository.deleteById(id);

        return "redirect:/cash";
    }
}