export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
    id?: number;
    amount: number;
    description?: string;
    dateTime?: string;
    type: TransactionType | string;

    // Single account context
    accountId?: number;

    // Transfer context
    sourceAccountId?: number;
    destinationAccountId?: number;
}
