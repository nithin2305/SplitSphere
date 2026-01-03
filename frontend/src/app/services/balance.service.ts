import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Balance } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BalanceService {
  private apiUrl = `${environment.apiUrl}/balances`;

  constructor(private http: HttpClient) { }

  getGroupBalances(groupId: number): Observable<Balance[]> {
    return this.http.get<Balance[]>(`${this.apiUrl}/group/${groupId}`);
  }
}
