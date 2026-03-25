export interface User {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: 'student' | 'admin';
  avatar?: string;
  balance: number;
  points: number;
  creditScore?: number;
  verified?: boolean;
  university?: string;
}

export interface Textbook {
  id: string;
  title: string;
  author: string;
  isbn: string;
  publisher?: string;
  price: number;
  originalPrice?: number;
  dailyRent?: number;
  rentEnabled?: boolean;
  description?: string;
  condition: string;
  category: string;
  cover?: string;
  sellerId: string;
  status: 'available' | 'sold' | 'rented' | 'donated' | 'off_shelf';
  type?: 'sell' | 'rent' | 'donate';
  createdAt: string;
  seller?: User;
}

export interface Order {
  id: string;
  textbookId: string;
  textbookTitle: string;
  buyerId: string;
  sellerId: string;
  amount: number;
  type: 'buy' | 'rent' | 'donate';
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled';
  createdAt: string;
  cover?: string;
  addressId?: string;
}

export interface Address {
  id: string;
  recipient: string;
  phone: string;
  detail: string;
  isDefault?: boolean;
}

export interface Post {
  id: number;
  userId: string;
  title: string;
  content: string;
  type: 'question' | 'transaction' | 'review' | 'discussion';
  views: number;
  likes: number;
  commentsCount?: number;
  createdAt: string;
  updatedAt: string;
  user?: User;
}

export interface Comment {
  id: number;
  postId: number;
  userId: string;
  content: string;
  createdAt: string;
  user?: User;
}
