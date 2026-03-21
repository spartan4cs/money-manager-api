import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';
import { Transaction } from '../models/transaction.model';

@Injectable({
    providedIn: 'root'
})
export class ApiService {

    // Endpoints will be proxied to the backend via proxy.conf.json
    private readonly baseUrl = '/api';

    constructor(private http: HttpClient) { }

    // ----------------------------------------------------------------------
    // Accounts
    // ----------------------------------------------------------------------

    getAccounts(): Observable<Account[]> {
        return this.http.get<Account[]>(`${this.baseUrl}/accounts`);
    }

    getAccount(id: number): Observable<Account> {
        return this.http.get<Account>(`${this.baseUrl}/accounts/${id}`);
    }

    createAccount(account: Account): Observable<Account> {
        return this.http.post<Account>(`${this.baseUrl}/accounts`, account);
    }

    updateAccount(id: number, account: Partial<Account>): Observable<Account> {
        return this.http.put<Account>(`${this.baseUrl}/accounts/${id}`, account);
    }

    deleteAccount(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/accounts/${id}`);
    }

    getSharedAccountTypes(): Observable<string[]> {
        return this.http.get<string[]>(`${this.baseUrl}/accounts/types/available`);
    }

    getBalance(accountId: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}/accounts/balance/${accountId}`);
    }

    // ----------------------------------------------------------------------
    // Transactions
    // ----------------------------------------------------------------------

    getTransactions(): Observable<Transaction[]> {
        return this.http.get<Transaction[]>(`${this.baseUrl}/transactions`);
    }

    getTransaction(id: number): Observable<Transaction> {
        return this.http.get<Transaction>(`${this.baseUrl}/transactions/${id}`);
    }

    createTransaction(transaction: Transaction): Observable<Transaction> {
        return this.http.post<Transaction>(`${this.baseUrl}/transactions`, transaction);
    }

    updateTransaction(id: number, transaction: Partial<Transaction>): Observable<Transaction> {
        return this.http.put<Transaction>(`${this.baseUrl}/transactions/${id}`, transaction);
    }

    deleteTransaction(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/transactions/${id}`);
    }

    getTransactionsByType(type: string): Observable<Transaction[]> {
        return this.http.get<Transaction[]>(`${this.baseUrl}/transactions/by-type/${type}`);
    }

    getTransactionsByAccount(accountId: number): Observable<Transaction[]> {
        return this.http.get<Transaction[]>(`${this.baseUrl}/transactions/account/${accountId}`);
    }
}
