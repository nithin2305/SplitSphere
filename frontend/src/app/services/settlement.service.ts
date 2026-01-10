import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Settlement, SettlementRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class SettlementService {
  private apiUrl = `${environment.apiUrl}/settlements`;

  constructor(private http: HttpClient) {}

  createSettlement(request: SettlementRequest): Observable<Settlement> {
    return this.http.post<Settlement>(this.apiUrl, request);
  }

  getGroupSettlements(groupId: number): Observable<Settlement[]> {
    return this.http.get<Settlement[]>(`${this.apiUrl}/group/${groupId}`);
  }
}
