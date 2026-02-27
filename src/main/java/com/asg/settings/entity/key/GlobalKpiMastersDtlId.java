package com.asg.settings.entity.key;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKpiMastersDtlId implements Serializable {

    private Long transactionPoid;
    private Long detRowId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalKpiMastersDtlId)) return false;
        GlobalKpiMastersDtlId that = (GlobalKpiMastersDtlId) o;
        return Objects.equals(transactionPoid, that.transactionPoid) &&
                Objects.equals(detRowId, that.detRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detRowId);
    }
}