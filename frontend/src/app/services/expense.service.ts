import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense, ExpenseRequest } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private apiUrl = `${environment.apiUrl}/expenses`;

  constructor(private http: HttpClient) { }

  createExpense(expenseRequest: ExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expenseRequest);
  }

  getGroupExpenses(groupId: number): Observable<Expense[]> {
    return this.http.get<Expense[]>(`${this.apiUrl}/group/${groupId}`);
  }
}
