import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Item } from './item';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ItemService {
  public filteredItems: Item[];

  // The URL for the items part of the server API
  readonly itemUrl: string = `${environment.apiUrl}items/get`;
  readonly newItemUrl: string = `${environment.apiUrl}items/add`;
  private readonly selKey = 'selections';
  constructor(private httpClient: HttpClient) {
  }

  getItems(filters?: { name?: string }): Observable<Item[]> {
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters.name) {
        httpParams = httpParams.set('name', filters.name);
      }
    }
    return this.httpClient.get<Item[]>(this.itemUrl, {
      params: httpParams,
    });
  }

  filterItems(items: Item[]): Item[] {
    const filteredItems = items;

    return filteredItems;
  }

  addItem(newItem: Partial<Item>): Observable<string> {
    // Send post item to add a new Item with the Item data as the body.
    return this.httpClient.post<{ id: string }>(this.newItemUrl, newItem).pipe(map(res => res.id));
  }
}

  // readonly itemUrl: string = environment.apiUrl + 'items';
  // constructor(private httpClient: HttpClient) {
  // }

  // getItems(filters?: { itemName?: string; unit?: string; amount?: string }): Observable<Item[]> {

  //   let httpParams: HttpParams = new HttpParams();
  //   if (filters) {
  //     if (filters.itemName) {
  //       httpParams = httpParams.set('itemName', filters.itemName);
  //     }
  //     if (filters.unit) {
  //       httpParams = httpParams.set('unit', filters.unit);
  //     }
  //     if (filters.amount) {
  //       httpParams = httpParams.set('amount', filters.amount);
  //     }
  //   }
  //   return this.httpClient.get<Item[]>(this.itemUrl, {
  //     params: httpParams,
  //   });
  // }

  // getItemById(id: string): Observable<Item>{
  //   return this.httpClient.get<Item>(this.itemUrl +'/' + id);
  // }

  // addItem(newItem: Item): Observable<string> {
  //   return this.httpClient.post<{id: string}>(this.itemUrl, newItem).pipe(map(res => res.id));
  // }

