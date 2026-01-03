export interface User {
  id?: number;
  accountName: string;
  userId: string;
}

export interface UserRegistration {
  accountName: string;
  userId: string;
  code: string;
}

export interface LoginRequest {
  userId: string;
  code: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  accountName: string;
}

export interface Group {
  id: number;
  name: string;
  joinCode: string;
  creatorName: string;
  memberNames: string[];
  createdAt: string;
}

export interface GroupRequest {
  name: string;
}

export interface Expense {
  id: number;
  description: string;
  amount: number;
  payerName: string;
  payerUserId: string;
  participantNames: string[];
  perPersonAmount: number;
  createdAt: string;
}

export interface ExpenseRequest {
  description: string;
  amount: number;
  groupId: number;
  participantUserIds: string[];
}

export interface Balance {
  userId: string;
  userName: string;
  balance: number;
  status: string;
}
