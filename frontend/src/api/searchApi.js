import { api } from './index';

export const searchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    searchEntities: builder.query({
      query: ({ q, type = 'ALL' }) => ({
        url: `/api/v1/search?q=${encodeURIComponent(q)}&type=${type}`,
        method: 'GET',
      }),
    }),
  }),
});

export const {
  useSearchEntitiesQuery,
  useLazySearchEntitiesQuery,
} = searchApi;
export default searchApi;
