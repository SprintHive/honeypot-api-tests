package com.sprinthive.origination.model;

import lombok.Value;

import java.util.Arrays;
import java.util.Optional;

@Value
public class FileMetadata {
    private String id;
    private String name;
    private FileType type;

    public enum FileType {
        BANK_STATEMENT("bank-statement"),
        DRIVERS_LICENSE("drivers-license"),
        ID_BOOK("id-book");

        private String typeCode;

        FileType(String typeCode) {
            this.typeCode = typeCode;
        }

        public String getTypeCode() {
            return typeCode;
        }

        public static FileType fromTypeCode(String typeCode) {
            Optional<FileType> optionalType = Arrays
                    .stream(values())
                    .filter(fileType -> fileType.typeCode.equals(typeCode))
                    .findAny();
            return optionalType.orElseThrow(() -> new RuntimeException("Invalid typeCode"));
        }
    }
}

