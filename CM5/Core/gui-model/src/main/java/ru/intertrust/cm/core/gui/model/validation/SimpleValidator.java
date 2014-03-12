package ru.intertrust.cm.core.gui.model.validation;

import ru.intertrust.cm.core.business.api.dto.Constraint;

import java.util.HashMap;
import java.util.Map;

/**
 * Валидатор, позволяющий проводить проверку данных по заранее предопределенным словам (not-empty, positive-int etc)
 * или по регулярному выражению.
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 15:28
 */
public class SimpleValidator extends AbstractValidator {

    private final String pattern;
    private final String messageKey;
    private final String wordOrPattern;

    public SimpleValidator(Constraint constraint) {
        wordOrPattern = constraint.param(Constraint.PARAM_PATTERN);
        String pattern = wordsToPatterns.get(wordOrPattern);
        String messageKey = "validate." + wordOrPattern;
        if (pattern == null) {
            pattern = wordOrPattern;
            messageKey = "validate.pattern";
        }
        this.pattern = pattern;
        this.messageKey = messageKey;
    }

    private static final Map<String, String> wordsToPatterns = new HashMap<String, String>();

    static {
        wordsToPatterns.put(Constraint.KEYWORD_NOT_EMPTY, "^.*[^\\s]+.*$");
        wordsToPatterns.put(Constraint.KEYWORD_INTEGER, "^([-+]?\\d+)?$");
        wordsToPatterns.put(Constraint.KEYWORD_POSITIVE_INT, "^([+]?\\d+)?$");
        wordsToPatterns.put(Constraint.KEYWORD_DECIMAL, "^([-+]?[0-9]+(\\.[0-9]+)?)?$");
        wordsToPatterns.put(Constraint.KEYWORD_POSITIVE_DEC, "^([+]?[0-9]+(\\.[0-9]+)?)?$");
    }

    @Override
    void doValidation(CanBeValidated canBeValidated, ValidationResult validationResult) {
        String value = (String) canBeValidated.getValue();
        if (value != null && pattern!= null && !value.matches(pattern)) {
            validationResult.addError(messageKey);
        }
    }

    @Override
    public String toString() {
        return "Client simple validator: " + wordOrPattern;
    }
}
