import { Component, OnInit } from '@angular/core';
import { Router,NavigationEnd  } from '@angular/router';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent  {
  title = 'PocketShelf';

  constructor(private router: Router){}

}
