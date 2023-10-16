package org.example.utils.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class ShortCircuitValidator<T> {

    private final Supplier<T> valueSupplier;
    private final List<ValidationBlock<T>> validationBlocks;
    private final List<Supplier<List<String>>> previousValidators;

    public static <T> ShortCircuitValidator<T> of(T value) {
        return new ShortCircuitValidator<>(() -> value, new ArrayList<>());
    }

    private ShortCircuitValidator(Supplier<T> valueSupplier, List<Supplier<List<String>>> previousValidators) {
        this.valueSupplier = valueSupplier;
        this.validationBlocks = new ArrayList<>();
        this.previousValidators = previousValidators;
    }

    public ShortCircuitValidator<T> addValidationBlock(ValidationBlock<T> validationBlock) {
        validationBlocks.add(validationBlock);
        return this;
    }

    public <U> ShortCircuitValidator<U> addTransformationBlock(TransformationBlock<T, U> transformationBlock) {
        previousValidators.add(tryValidate()::errors);
        return new ShortCircuitValidator<>(() -> transformationBlock.apply(valueSupplier.get()), previousValidators);
    }

    public ValidationResult<T> tryValidate() {

        try {
            if (!previousValidators.isEmpty()) {
                for (var previousValidator : previousValidators) {
                    var result = previousValidator.get();
                    if (!result.isEmpty()) {
                        return new ValidationResult<>(empty(), result, empty());
                    }
                }
            }

            final var validationContext = new ValidationContext<>(valueSupplier.get(), new ArrayList<>());
            for (ValidationBlock<T> validationBlock : validationBlocks) {
                validationBlock.consume(validationContext);
                if (!validationContext.errors.isEmpty()) {
                    return new ValidationResult<>(empty(), validationContext.errors, empty());
                }
            }

            return new ValidationResult<T>(Optional.of(validationContext.getVal()), emptyList(), empty());
        } catch (Exception ex) {
            return new ValidationResult<T>(Optional.empty(), emptyList(), Optional.of(ex));
        }

    }

    @FunctionalInterface
    public interface ValidationBlock<T> {
        void consume(ValidationContext<T> validationContext);
    }

    @FunctionalInterface
    public interface TransformationBlock<T, U> {
        U apply(T value);
    }

    public record ValidationResult<T>(
          Optional<T> validatedValueOpt,
          List<String> errors,
          Optional<Exception> exceptionOpt
    ) {

        public T getOrThrow() {
            return validatedValueOpt.orElseThrow();
        }

        public boolean isSuccessful() {
            return validatedValueOpt.isPresent();
        }

        public boolean hasFailed() {
            return !isSuccessful();
        }
    }

    public static final class ValidationContext<T> {

        private final T value;
        private final List<String> errors;

        public ValidationContext(T value, List<String> errors) {
            this.value = value;
            this.errors = errors;
        }

        public T getVal() {
            return value;
        }

        public void ifNull(Object obj, String error) {
            if (obj == null) {
                errors.add(error);
            }
        }

        public void ifEmpty(String str, String error) {
            if (str.isEmpty()) {
                errors.add(error);
            }
        }

        public void ifFalse(boolean condition, String error) {
            if (!condition) {
                errors.add(error);
            }
        }

        public void ifTrue(boolean condition, String error) {
            if (condition) {
                errors.add(error);
            }
        }

        public void ifDuplicateValuesExist(Collection<String> values, String error) {
            if(new HashSet<>(values).size() != values.size()) {
                errors.add(error);
            }
        }

        public void ifDuplicateValuesExist(Collection<String> values, Consumer<Set<String>> errorHandler) {

            final var valueFrequencies = values.stream().collect(groupingBy(identity()));
            final var duplicates = valueFrequencies
                  .entrySet()
                  .stream()
                  .filter(e -> e.getValue().size() > 1)
                  .map(Map.Entry::getKey)
                  .collect(toSet());

            if (!duplicates.isEmpty()) {
                errorHandler.accept(duplicates);
            }
        }

        public void error(String error) {
            errors.add(error);
        }

    }
}
