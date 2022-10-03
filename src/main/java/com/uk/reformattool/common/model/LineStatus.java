package com.uk.reformattool.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineStatus {
    private boolean isLineCommented;
    private boolean isBulkCommented;

    public boolean isCommented() {
        return isLineCommented || isBulkCommented;
    }
}
