import { api } from './index';

export const mediaApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getUploadUrl: builder.mutation({
      query: (data) => ({
        url: '/api/v1/media/upload-url',
        method: 'POST',
        data, // { fileName, contentType, fileSize }
      }),
    }),
    confirmUpload: builder.mutation({
      query: (mediaId) => ({
        url: '/api/v1/media/confirm',
        method: 'POST',
        data: { mediaId },
      }),
    }),
    getMediaDetails: builder.query({
      query: (mediaId) => ({
        url: `/api/v1/media/${mediaId}`,
        method: 'GET',
      }),
    }),
  }),
});

export const {
  useGetUploadUrlMutation,
  useConfirmUploadMutation,
  useGetMediaDetailsQuery,
} = mediaApi;
export default mediaApi;
