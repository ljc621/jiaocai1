import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../components/Layout.vue'

const router = createRouter({
  history: createWebHistory((import.meta as any).env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('../views/admin/AdminDashboard.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/AdminUserList.vue')
        },
        {
          path: 'textbooks',
          name: 'admin-textbooks',
          component: () => import('../views/admin/AdminTextbookList.vue')
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/AdminOrderList.vue')
        },
        {
          path: 'community',
          name: 'admin-community',
          component: () => import('../views/admin/AdminPostList.vue')
        },
        {
          path: 'reviews',
          name: 'admin-reviews',
          component: () => import('../views/admin/AdminReviewList.vue')
        }
      ]
    },
    {
      path: '/',
      component: Layout,
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('../views/Home.vue')
        },
        {
          path: 'textbooks',
          name: 'textbooks',
          component: () => import('../views/TextbookList.vue')
        },
        {
          path: 'textbooks/create',
          name: 'textbook-create',
          component: () => import('../views/TextbookCreate.vue')
        },
        {
          path: 'textbooks/edit/:id',
          name: 'textbook-edit',
          component: () => import('../views/TextbookCreate.vue')
        },
        {
          path: 'textbooks/:id',
          name: 'textbook-detail',
          component: () => import('../views/TextbookDetail.vue')
        },
        {
          path: 'posts',
          name: 'posts',
          component: () => import('../views/PostList.vue')
        },
        {
          path: 'posts/create',
          name: 'post-create',
          component: () => import('../views/PostCreate.vue')
        },
        {
          path: 'posts/:id',
          name: 'post-detail',
          component: () => import('../views/PostDetail.vue')
        },
        {
          path: 'orders/create',
          name: 'order-create',
          component: () => import('../views/OrderCreate.vue')
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/OrderList.vue')
        },
        {
          path: 'orders/:id',
          name: 'order-detail',
          component: () => import('../views/OrderDetail.vue')
        },
        {
          path: 'addresses',
          name: 'addresses',
          component: () => import('../views/AddressList.vue')
        },
        {
          path: 'profile',
          component: () => import('../views/UserCenter.vue'),
          children: [
            {
              path: '',
              name: 'user-dashboard',
              component: () => import('../views/UserDashboard.vue')
            },
            {
              path: 'info',
              name: 'user-info',
              component: () => import('../views/UserProfile.vue')
            },
            {
              path: 'orders',
              name: 'user-orders',
              component: () => import('../views/OrderList.vue')
            },
            {
              path: 'published',
              name: 'user-published',
              component: () => import('../views/UserPublished.vue')
            },
            {
              path: 'favorites',
              name: 'user-favorites',
              component: () => import('../views/UserFavorites.vue')
            },
            {
              path: 'wallet',
              name: 'user-wallet',
              component: () => import('../views/UserWallet.vue')
            },
            {
              path: 'points',
              name: 'user-points',
              component: () => import('../views/UserPoints.vue')
            },
            {
              path: 'addresses',
              name: 'user-addresses',
              component: () => import('../views/AddressList.vue')
            },
            {
              path: 'settings',
              name: 'user-settings',
              component: () => import('../views/UserSettings.vue')
            }
          ]
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/Cart.vue')
        },
        {
          path: 'messages',
          name: 'messages',
          component: () => import('../views/Messages.vue')
        }
      ]
    }
  ]
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user');

  // Define routes that require authentication
  const protectedRoutes = [
    '/profile', 
    '/orders', 
    '/textbooks/create', 
    '/posts/create', 
    '/addresses', 
    '/cart'
  ];

  // Check if the current route matches any protected route
  // We use exact match for 'create' routes to avoid blocking 'detail' routes if they shared a prefix (though they don't here)
  // But for '/orders', we want to block /orders, /orders/create, /orders/:id
  const isProtected = protectedRoutes.some(route => {
    return to.path === route || to.path.startsWith(route + '/');
  });
  
  if (isProtected && !user) {
    next('/login');
  } else {
    next();
  }
});

export default router
