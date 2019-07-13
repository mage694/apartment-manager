import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/home.vue'
import ApartmentList from '@/components/apartments/apartment-list.vue'
import ApartmentForm from '@/components/apartments/apartment-form.vue'
import ApartmentUpdateForm from '@/components/apartments/apartment-update-form.vue'
import CustomerList from '@/components/customers/customer-list.vue'
import CustomerUpdateForm from '@/components/customers/customer-update-form.vue'
import PaymentList from '@/components/payments/payment-list.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
      children: [
        {
          path: '/apartments',
          name: 'ApartmentList',
          component: ApartmentList
        },
        {
          path: '/apartmentForm',
          name: 'ApartmentForm',
          component: ApartmentForm
        },
        {
          path: '/apartmentForm/:id',
          name: 'ApartmentUploadForm',
          component: ApartmentUpdateForm
        },
        {
          path: '/payments',
          name: 'PaymentList',
          component: PaymentList
        },
        {
          path: '/customers',
          name: 'CustomerList',
          component: CustomerList
        },
        {
          path: '/customerForm/:id',
          name: 'CustomerUpdateForm',
          component: CustomerUpdateForm
        }
      ]
    }
  ]
})
