import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/users`;

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient) { }

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(API_URL);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/${id}`);
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(API_URL, user);
  }

  update(id: number, user: User): Observable<User> {
    return this.http.put<User>(`${API_URL}/${id}`, user);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${API_URL}/${id}`);
  }
}
