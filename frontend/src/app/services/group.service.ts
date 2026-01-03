import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Group, GroupRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private apiUrl = 'http://localhost:8080/api/groups';

  constructor(private http: HttpClient) { }

  createGroup(groupRequest: GroupRequest): Observable<Group> {
    return this.http.post<Group>(this.apiUrl, groupRequest);
  }

  joinGroup(joinCode: string): Observable<Group> {
    return this.http.post<Group>(`${this.apiUrl}/join/${joinCode}`, {});
  }

  getUserGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(this.apiUrl);
  }

  getGroup(groupId: number): Observable<Group> {
    return this.http.get<Group>(`${this.apiUrl}/${groupId}`);
  }
}
