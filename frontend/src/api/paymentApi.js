import { api } from './index';

export const paymentApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPaymentPlans: builder.query({
      query: () => ({
        url: '/api/v1/payments/plans',
        method: 'GET',
      }),
    }),
    createCheckoutSession: builder.mutation({
      query: (data) => ({
        url: '/api/v1/payments/checkout',
        method: 'POST',
        data, // { planId, planName, amount }
      }),
    }),
    getSubscriptionDetails: builder.query({
      query: () => ({
        url: '/api/v1/payments/subscription',
        method: 'GET',
      }),
      providesTags: ['Profile'],
    }),
  }),
});

export const {
  useGetPaymentPlansQuery,
  useCreateCheckoutSessionMutation,
  useGetSubscriptionDetailsQuery,
} = paymentApi;
export default paymentApi;
