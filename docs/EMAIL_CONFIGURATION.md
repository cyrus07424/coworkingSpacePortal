# Email Configuration Guide

## Overview
The coworking space portal now supports email functionality for user registration and password reset. This document explains how to configure the email service.

## Environment Variables

To enable email functionality, set the following environment variables:

### Required Variables
- `SMTP_HOST`: SMTP server hostname (e.g., smtp.gmail.com)
- `SMTP_USERNAME`: SMTP authentication username 
- `SMTP_PASSWORD`: SMTP authentication password

### Optional Variables
- `SMTP_PORT`: SMTP server port (default: 587)
- `SMTP_AUTH`: Enable SMTP authentication (default: true)
- `SMTP_STARTTLS`: Enable STARTTLS encryption (default: true)
- `FROM_EMAIL`: Email address used as sender (default: noreply@coworkingspace.local)
- `FROM_NAME`: Name used as sender (default: コワーキングスペースポータル)

## Gmail Configuration Example

For Gmail SMTP:
```bash
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
export FROM_EMAIL=your-email@gmail.com
export FROM_NAME="Coworking Space Portal"
```

Note: For Gmail, you'll need to use an App Password instead of your regular password.

## Features

### User Registration Email
- Automatically sends welcome email when new users register
- Email includes user details and welcome message
- Gracefully handles cases where email service is not configured

### Password Reset Flow
1. User clicks "パスワードを忘れた場合" on login page
2. User enters email address
3. System generates secure token and sends email with reset link  
4. User clicks link and sets new password
5. Token expires after 24 hours and can only be used once

### Security Features
- Password reset tokens expire after 24 hours
- Tokens are single-use only
- Email addresses are not revealed if they don't exist in the system
- Password reset invalidates all existing tokens for the user

## Testing
If email service is not configured, the system will:
- Log a message indicating email service is not configured
- Continue normal operation without sending emails
- Not throw exceptions or fail registration/password reset processes

This allows development and testing without requiring SMTP configuration.