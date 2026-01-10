import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Transaction } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  getGroupTransactions(groupId: number): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/group/${groupId}`);
  }
}
