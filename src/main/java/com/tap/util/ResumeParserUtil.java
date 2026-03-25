package com.tap.util;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class ResumeParserUtil {

    // ─── Extract Raw Text via Apache Tika ───────────────────────────
    public static String extractText(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        tika.setMaxStringLength(-1);
        return tika.parseToString(file.getInputStream());
    }
}