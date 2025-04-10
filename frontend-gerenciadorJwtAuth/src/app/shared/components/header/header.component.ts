import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../../core/auth/token-storage.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  constructor(
    private tokenStorage: TokenStorageService,
    private router: Router
  ) { }

  get isLoggedIn(): boolean {
    return !!this.tokenStorage.getToken();
  }

  get username(): string {
    const user = this.tokenStorage.getUser();
    return user ? user.username : '';
  }

  logout(): void {
    this.tokenStorage.signOut();
    this.router.navigate(['/login']);
  }
}
