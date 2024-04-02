/**
 * Form validation utility functions.
 * Provides reusable validators for common form fields.
 */

/**
 * Validates an email address.
 * @param {string} email - The email to validate
 * @returns {boolean} True if valid
 */
export const isValidEmail = (email) => {
    if (!email) return false;
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
};

/**
 * Validates a Vietnamese phone number.
 * @param {string} phone - The phone number to validate
 * @returns {boolean} True if valid
 */
export const isValidPhone = (phone) => {
    if (!phone) return false;
    const regex = /^(\+84|0)(3|5|7|8|9)[0-9]{8}$/;
    return regex.test(phone.replace(/\s/g, ''));
};

/**
 * Validates password strength.
 * @param {string} password - The password to validate
 * @returns {object} Validation result with strength score
 */
export const validatePassword = (password) => {
    const result = {
        isValid: false,
        score: 0,
        errors: [],
    };

    if (!password || password.length < 6) {
        result.errors.push('Password must be at least 6 characters');
        return result;
    }

    if (password.length >= 8) result.score++;
    if (/[A-Z]/.test(password)) result.score++;
    if (/[0-9]/.test(password)) result.score++;
    if (/[^A-Za-z0-9]/.test(password)) result.score++;

    result.isValid = result.score >= 2;
    return result;
};

/**
 * Validates that a field is not empty.
 * @param {string} value - The value to check
 * @param {string} fieldName - Name of the field for error message
 * @returns {string|null} Error message or null if valid
 */
export const required = (value, fieldName = 'This field') => {
    if (!value || (typeof value === 'string' && !value.trim())) {
        return `${fieldName} is required`;
    }
    return null;
};

export default {
    isValidEmail,
    isValidPhone,
    validatePassword,
    required,
};
