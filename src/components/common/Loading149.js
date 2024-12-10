import React from 'react';

/**
 * Loading spinner component.
 * Displays a centered loading indicator with optional message.
 */
const Loading149 = ({ message = 'Loading...', size = 'medium' }) => {
    const sizeMap = {
        small: { width: 24, height: 24, border: 3 },
        medium: { width: 40, height: 40, border: 4 },
        large: { width: 60, height: 60, border: 5 },
    };

    const dimensions = sizeMap[size] || sizeMap.medium;

    const spinnerStyle = {
        width: dimensions.width,
        height: dimensions.height,
        border: `${dimensions.border}px solid #f3f3f3`,
        borderTop: `${dimensions.border}px solid #3498db`,
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
        margin: '0 auto',
    };

    const containerStyle = {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px 20px',
    };

    return (
        <div style={containerStyle}>
            <div style={spinnerStyle} />
            {message && (
                <p style={{ marginTop: 16, color: '#666', fontSize: 14 }}>
                    {message}
                </p>
            )}
            <style>
                {`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}
            </style>
        </div>
    );
};

export default Loading149;
