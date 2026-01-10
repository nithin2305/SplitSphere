import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  downloadExcelReport(groupId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/group/${groupId}/excel`, {
      responseType: 'blob'
    });
  }

  downloadPdfReport(groupId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/group/${groupId}/pdf`, {
      responseType: 'blob'
    });
  }
}
