import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats a number as Bosnian Mark (BAM) currency.
 * Usage: {{ price | currencyBam }}
 * Output: "25,99 KM"
 */
@Pipe({
  name: 'currencyBam',
  standalone: true,
})
export class CurrencyBamPipe implements PipeTransform {
  transform(value: number | null | undefined, showSymbol = true): string {
    if (value == null) return showSymbol ? '0,00 KM' : '0,00';

    const formatted = value.toLocaleString('bs-BA', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

    return showSymbol ? `${formatted} KM` : formatted;
  }
}
