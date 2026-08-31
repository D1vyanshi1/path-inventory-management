package com.path.inventory.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpData {

        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(
                String otp,
                LocalDateTime expiryTime) {

            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }


    /*
     * Existing Forgot Password OTP storage
     */
    private final Map<String, OtpData> otpStorage =
            new ConcurrentHashMap<>();


    /*
     * Separate Login OTP storage
     */
    private final Map<String, OtpData> loginOtpStorage =
            new ConcurrentHashMap<>();


    private String createOtp() {

        return String.format(
                "%06d",
                new Random().nextInt(1000000)
        );

    }


    /* =========================================
       FORGOT PASSWORD OTP
       ========================================= */

    public String generateOtp(String email) {

        String otp = createOtp();

        LocalDateTime expiryTime =
                LocalDateTime.now()
                        .plusMinutes(5);

        otpStorage.put(
                email,
                new OtpData(
                        otp,
                        expiryTime
                )
        );

        return otp;
    }


    public boolean verifyOtp(
            String email,
            String otp) {

        OtpData otpData =
                otpStorage.get(email);

        if (otpData == null) {

            return false;
        }

        if (
                LocalDateTime.now()
                        .isAfter(
                                otpData.expiryTime
                        )
        ) {

            otpStorage.remove(email);

            return false;
        }

        if (!otpData.otp.equals(otp)) {

            return false;
        }

        /*
         * Remove OTP after successful
         * verification.
         */
        otpStorage.remove(email);

        return true;
    }


    public void removeOtp(String email) {

        otpStorage.remove(email);

    }


    /* =========================================
       LOGIN OTP
       ========================================= */

    public String generateLoginOtp(String email) {

        String otp = createOtp();

        LocalDateTime expiryTime =
                LocalDateTime.now()
                        .plusMinutes(5);

        loginOtpStorage.put(
                email,
                new OtpData(
                        otp,
                        expiryTime
                )
        );

        return otp;
    }


    public boolean verifyLoginOtp(
            String email,
            String otp) {

        OtpData otpData =
                loginOtpStorage.get(email);

        if (otpData == null) {

            return false;
        }

        if (
                LocalDateTime.now()
                        .isAfter(
                                otpData.expiryTime
                        )
        ) {

            loginOtpStorage.remove(email);

            return false;
        }

        if (!otpData.otp.equals(otp)) {

            return false;
        }

        /*
         * OTP can only be used once.
         */
        loginOtpStorage.remove(email);

        return true;
    }


    public void removeLoginOtp(String email) {

        loginOtpStorage.remove(email);

    }

}