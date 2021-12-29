package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.FieldConversionException;

import javax.validation.constraints.NotNull;
import java.util.*;

public class PersistedEntityRecord extends EntityRecord {

    private Map<String, Object> lastLk;
    private List<String> changedLkFields;

    public PersistedEntityRecord(@NotNull Entity entity) {
        super(entity);
        this.lastLk = new HashMap<>();
        this.changedLkFields = new ArrayList<>();
    }

    @Override
    public Optional<Map.Entry<Field, Object>> set(@NotNull String field, Object value) throws FieldConversionException {
        Optional<Map.Entry<Field, Object>> ret = super.set(field, value);
        if (ret.isPresent()) {
            Field ft = ret.get().getKey();
            Object storedValue = ret.get().getValue();
            if (this.getEntity().isLk(field)) {
                if (this.lastLk.containsKey(field) && !this.lastLk.get(field).equals(storedValue))
                    this.changedLkFields.add(ft.getName());
                else
                    lastLk.put(ft.getName(), storedValue);
            }
        }

        return ret;
    }

    public String getLastLkString() throws FieldConversionException {
        return getLastLkString(this.getEntity().getLkSeparator());
    }

    public String getLastLkString(String separator) throws FieldConversionException {
        StringBuilder res = new StringBuilder();
        boolean isFirst = true;
        for (Field lkField : this.getEntity().getLkFields()) {
            Object fieldOriginalValue = lastLk.get(lkField.getName());
            if (fieldOriginalValue == null)
                fieldOriginalValue = super.getData().get(lkField.getName());
            if(fieldOriginalValue != null) {
                String stValue = FieldConverter.toStringValue(lkField, fieldOriginalValue, separator);
                if (!stValue.isEmpty()) {
                    if (!isFirst)
                        res.append(separator);
                    res.append(stValue);
                    isFirst = false;
                }
            }
        }
        return res.toString();
    }

    public List<String> getChangedLkFields() {
        return List.copyOf(changedLkFields);
    }

    public Map<String, Object> getLastLk() {
        return Map.copyOf(this.lastLk);
    }

    public static EntityRecord from(EntityRecord rec) throws FieldConversionException {
        PersistedEntityRecord persistedEntityRecord = new PersistedEntityRecord(rec.getEntity());
        persistedEntityRecord.set(rec.getData());
        return persistedEntityRecord;
    }

}
