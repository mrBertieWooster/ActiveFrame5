package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.FieldPathOnDeleteActionConverter;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "field-path")
@Convert(FieldPathOnDeleteActionConverter.class)
public class FieldPathConfig implements Dto {
    public static final String CASCADE_STRING = "cascade";
    public static final String UNLINK_STRING = "unlink";
    public static final String CASCADE_1_TO_1_BACK_LINKS_STRING = "cascade 1:1 back-links";

    public static enum OnDeleteAction {
        CASCADE(CASCADE_STRING),
        UNLINK(UNLINK_STRING),
        CASCADE_1_TO_1_BACKLINKS(CASCADE_1_TO_1_BACK_LINKS_STRING);

        private final String string;

        private OnDeleteAction() {
            string = null;
        }

        private OnDeleteAction(String str) {
            this.string = str;
        }

        public String getString() {
            return string;
        }

        public static OnDeleteAction getEnum(String name) {
            switch (name) {
                case CASCADE_STRING:
                    return CASCADE;
                case UNLINK_STRING:
                    return UNLINK;
                case CASCADE_1_TO_1_BACK_LINKS_STRING:
                    return CASCADE_1_TO_1_BACKLINKS;
            }
            return null;
        }
    }

    @Attribute(name = "value", required = false)
    private String value;

    @Attribute(name="on-delete", required = false)
    private OnDeleteAction onDelete;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OnDeleteAction getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(OnDeleteAction onDelete) {
        this.onDelete = onDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldPathConfig that = (FieldPathConfig) o;

        if (onDelete != that.onDelete) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (onDelete != null ? onDelete.hashCode() : 0);
        return result;
    }
}
