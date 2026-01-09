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

export interface Member {
  userId: string;
  accountName: string;
}

export interface Group {
  id: number;
  name: string;
  joinCode: string;
  creatorName: string;
  memberNames: string[];
  members: Member[];
  closed?: boolean;
  closedAt?: string;
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

export interface Settlement {
  id?: number;
  payerUserId: string;
  payerName: string;
  payeeUserId: string;
  payeeName: string;
  amount: number;
  note?: string;
  createdAt?: string;
}

export interface SettlementRequest {
  groupId: number;
  payeeUserId: string;
  amount: number;
  note?: string;
}

export interface Transaction {
  id: number;
  type: 'EXPENSE' | 'SETTLEMENT';
  description: string;
  amount: number;
  payerUserId: string;
  payerName: string;
  payeeUserId?: string;
  payeeName?: string;
  participantNames?: string;
  perPersonAmount?: number;
  note?: string;
  createdAt: string;
}
