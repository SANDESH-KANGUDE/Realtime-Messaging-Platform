import { createApi } from '@reduxjs/toolkit/query/react';
import apiClient from './client';

// Custom baseQuery wrapper that uses our configured Axios client
const axiosBaseQuery = () => async ({ url, method, data, params, headers }) => {
  try {
    const result = await apiClient({
      url,
      method,
      data,
      params,
      headers,
    });
    return { data: result.data };
  } catch (axiosError) {
    return {
      error: {
        status: axiosError.response?.status,
        data: axiosError.response?.data || axiosError.message,
      },
    };
  }
};

export const api = createApi({
  reducerPath: 'api',
  baseQuery: axiosBaseQuery(),
  tagTypes: ['Profile', 'Chats', 'Messages', 'Friends', 'Notifications'],
  endpoints: () => ({}),
});
