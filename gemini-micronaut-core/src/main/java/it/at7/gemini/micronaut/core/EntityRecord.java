package it.at7.gemini.micronaut.core;

import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityRecord {
    private final static Logger logger = LoggerFactory.getLogger(EntityRecord.class);

    private final Entity entity;
    private final Map<String, Object> store;

    public EntityRecord(@NotNull Entity entity) {
        this.entity = entity;
        CheckArgument.isNotNull(entity, "Entity record must provide Entity");
        this.store = new HashMap<>();
    }

    public Entity getEntity() {
        return entity;
    }

    public Map<String, Object> getData() {
        return Map.copyOf(this.store);
    }

    @Nullable
    public Object get(String field) {
        return store.get(field);
    }

    public String getLkString() throws FieldConversionException {
        return getLkString(entity.getLkSeparator());
    }

    public String getLkString(String separator) throws FieldConversionException {
        if (entity.isSingleRecord())
            return entity.getLkSingleRecValue();
        StringBuilder res = new StringBuilder();
        boolean isFirst = true;
        this.entity.getLkFields().iterator();
        for (Field lkField : this.entity.getLkFields()) {
            Object value = get(lkField.getName());
            if (value != null) {
                String stValue = FieldConverter.toStringValue(lkField, value, separator);
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

    public Optional<Map.Entry<Field, Object>> set(Field field, Object value) throws FieldConversionException {
        return set(field.getName(), value);
    }

    public void setIgnoringException(@NotNull String field, Object value) {
        try {
            set(field, value);
        } catch (FieldConversionException e) {
            logger.warn(String.format("Ignoring conversion errors for %s - %s - %s", this.entity.getName(), field, value));
        }
    }

    public Optional<Map.Entry<Field, Object>> set(@NotNull String field, Object value) throws FieldConversionException {
        CheckArgument.notEmpty(field, "must provide a field to set a record value");
        Optional<Field> fieldOpt = entity.getFieldOpt(field);
        if (fieldOpt.isPresent()) {
            Field ft = fieldOpt.get();
            Object storedValue = FieldConverter.toValue(ft, value);
            if (storedValue == null)
                store.remove(field);
            else {
                store.put(ft.getName(), storedValue);
                return Optional.of(Map.entry(ft, storedValue));
            }
        }
        return Optional.empty();
    }

    public void remove(@NotNull String field) {
        CheckArgument.notEmpty(field, "must provide a field to set a record value");
        Optional<Field> fieldOpt = entity.getFieldOpt(field);
        fieldOpt.ifPresent(value -> this.store.remove(value.getName()));
    }

    public void set(Map<String, Object> keyValues) throws FieldConversionException {
        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public void setIgnoringException(Map<String, Object> keyValues) {
        try {
            set(keyValues);
        } catch (FieldConversionException e) {
            logger.warn(String.format("Ignoring conversion errors for %s - %s - %s", this.entity.getName(), e.getField().getName(), e.getValue()));
        }
    }

    public void setLk(Object lk) throws FieldConversionException {
        List<Field> lkFields = this.entity.getLkFields();
        // TODO if it holds multiple fields need to check the object map
        if (lkFields.size() == 1) {
            this.set(lkFields.get(0).getName(), lk);
        }
    }

    public void setLkIgnoringException(Object lk) {
        try {
            setLk(lk);
        } catch (FieldConversionException e) {
            logger.warn(String.format("Ignoring conversion errors for %s - %s - %s", this.entity.getName(), e.getField().getName(), e.getValue()));
        }
    }

    public ValidationResult validate() {
        Map<String, Field.ValidationError> errors = new HashMap<>();
        for (Field field : this.entity.getFields().values()) {
            Optional<Field.ValidationError> fve = field.validate(store.get(field.getName()));
            fve.ifPresent(e -> errors.put(field.getName(), e));
        }
        return new ValidationResult(errors);
    }

    public static EntityRecord fromDataMap(Entity entity, Map<String, Object> data) throws FieldConversionException {
        EntityRecord entityRecord = new EntityRecord(entity);
        entityRecord.set(data);
        return entityRecord;
    }

    public static class ValidationResult {
        private final Map<String, Field.ValidationError> errors;

        public ValidationResult(Map<String, Field.ValidationError> errors) {
            CheckArgument.isNotNull(errors, "errors must be not null");
            this.errors = Map.copyOf(errors);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public Map<String, Field.ValidationError> getErrors() {
            return errors;
        }
    }
}
