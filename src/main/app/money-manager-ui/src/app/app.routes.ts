import { Routes } from '@angular/router';
import { Dashboard } from './components/dashboard/dashboard';
import { Accounts } from './components/accounts/accounts';
import { Transactions } from './components/transactions/transactions';

export const routes: Routes = [
    { path: '', component: Dashboard },
    { path: 'accounts', component: Accounts },
    { path: 'transactions', component: Transactions },
    { path: '**', redirectTo: '' }
];
