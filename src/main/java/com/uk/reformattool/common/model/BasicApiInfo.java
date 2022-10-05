package com.uk.reformattool.common.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model for csv serialization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicApiInfo {

    @CsvBindByName(column = "uk_file_context")
    private String ukFileContext;
    @CsvBindByName(column = "program_id")
    private String programId;
    @CsvBindByName(column = "url")
    private String url;

    private String programIdPrefix;
    private String programIdSuffix;
    private String screenId;

    public void setBasicData() {
        Pattern pattern = Pattern.compile("(\\w{3}).(\\d{3}|s\\d{2}).?(\\w)?");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            this.programIdPrefix = matcher.group(1);
            this.programIdSuffix = matcher.group(2);
            this.screenId = Optional.ofNullable(matcher.group(3)).orElse(".");
        }
    }

    public boolean equals(BasicApiInfo obj) {
        return this.programIdPrefix.equals(obj.programIdPrefix)
                && this.programIdSuffix.equals(obj.programIdSuffix)
                && this.screenId.equals(obj.screenId);
    }
}
