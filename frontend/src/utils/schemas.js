import * as z from 'zod';

export const loginSchema = z.object({
  identifier: z.string().min(1, 'Email or Phone Number is required'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = z.object({
  username: z.string()
    .min(3, 'Username must be at least 3 characters')
    .max(20, 'Username cannot exceed 20 characters')
    .regex(/^[a-zA-Z0-9_]+$/, 'Username can only contain alphanumeric characters and underscores'),
  email: z.string().email('Invalid email address format'),
  phoneNumber: z.string()
    .min(10, 'Phone number must be at least 10 characters')
    .max(15, 'Phone number cannot exceed 15 characters')
    .regex(/^\+?[1-9]\d{1,14}$/, 'Invalid phone format (e.g. +1234567890)'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const profileSchema = z.object({
  displayName: z.string().min(2, 'Display Name must be at least 2 characters'),
  username: z.string()
    .min(3, 'Username must be at least 3 characters')
    .max(20, 'Username cannot exceed 20 characters')
    .regex(/^[a-zA-Z0-9_]+$/, 'Username can only contain alphanumeric characters and underscores')
    .optional().or(z.literal('')),
  phoneNumber: z.string()
    .min(10, 'Phone number must be at least 10 characters')
    .regex(/^\+?[1-9]\d{1,14}$/, 'Invalid phone format (e.g. +1234567890)')
    .optional().or(z.literal('')),
  avatarUrl: z.string().url('Invalid profile photo URL format').optional().or(z.literal('')),
  bio: z.string().max(500, 'Bio must not exceed 500 characters').optional().or(z.literal('')),
  statusMessage: z.string().max(255, 'Status must not exceed 255 characters').optional().or(z.literal('')),
});

export const groupSchema = z.object({
  title: z.string().min(3, 'Group Title must be at least 3 characters').max(30, 'Group Title cannot exceed 30 characters'),
  avatarUrl: z.string().url('Invalid avatar URL format').or(z.literal('')).optional(),
});

export const pollSchema = z.object({
  question: z.string().min(5, 'Question must be at least 5 characters').max(100, 'Question cannot exceed 100 characters'),
  options: z.array(z.string().min(1, 'Option cannot be empty')).min(2, 'At least 2 options are required').max(10, 'Maximum 10 options allowed'),
});
