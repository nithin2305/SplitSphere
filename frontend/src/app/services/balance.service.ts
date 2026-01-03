import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Balance } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class BalanceService {
  private apiUrl = 'http://localhost:8080/api/balances';

  constructor(private http: HttpClient) { }

  getGroupBalances(groupId: number): Observable<Balance[]> {
    return this.http.get<Balance[]>(`${this.apiUrl}/group/${groupId}`);
  }
}
