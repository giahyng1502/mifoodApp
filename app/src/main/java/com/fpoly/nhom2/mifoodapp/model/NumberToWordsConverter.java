package com.fpoly.nhom2.mifoodapp.model;

public class NumberToWordsConverter {

    public static String convertToWords(long number) {
        if (number == 0) {
            return "0 Đồng";
        }

        StringBuilder words = new StringBuilder();

        if (number >= 1_000_000_000) {
            int billionPart = (int) (number / 1_000_000_000);
            words.append(billionPart).append(" Tỷ ");
            number %= 1_000_000_000;
        }

        if (number >= 1_000_000) {
            int millionPart = (int) (number / 1_000_000);
            words.append(millionPart).append(" Triệu ");
            number %= 1_000_000;
        }

        if (number >= 1_000) {
            int thousandPart = (int) (number / 1_000);
            words.append(thousandPart).append(" Nghìn ");
            number %= 1_000;
        }

        if (number > 0) {
            words.append(number);
        }

        words.append(" Đồng");

        return words.toString().trim();
    }

    public static void main(String[] args) {
        System.out.println(convertToWords(1650350000));  // Output: "1 Tỷ 650 Triệu 350 Nghìn VND"
    }
}
