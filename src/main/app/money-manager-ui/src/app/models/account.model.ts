export type AccountType = 'BANK' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'E_WALLET' | 'CASH' | 'SAVINGS' | 'INVESTMENT';

export interface Account {
    id?: number;
    name: string;
    type: AccountType | string;
    balance: number;
    accountNumber?: string;
    provider?: string;
    createdAt?: string;
    updatedAt?: string;
    description?: string;
    isActive?: boolean;
}
